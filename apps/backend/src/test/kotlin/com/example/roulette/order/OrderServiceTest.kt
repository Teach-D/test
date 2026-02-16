package com.example.roulette.order

import com.example.roulette.common.exception.BusinessException
import com.example.roulette.common.exception.ErrorCode
import com.example.roulette.order.dto.OrderRequest
import com.example.roulette.order.entity.Order
import com.example.roulette.order.entity.PointUsage
import com.example.roulette.point.PointService
import com.example.roulette.product.ProductService
import com.example.roulette.product.entity.Product
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class OrderServiceTest {
    private lateinit var orderRepository: OrderRepository
    private lateinit var pointUsageRepository: PointUsageRepository
    private lateinit var productService: ProductService
    private lateinit var pointService: PointService
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setUp() {
        orderRepository = mockk(relaxed = true)
        pointUsageRepository = mockk(relaxed = true)
        productService = mockk()
        pointService = mockk(relaxed = true)
        orderService = OrderService(orderRepository, pointUsageRepository, productService, pointService)
    }

    @Test
    fun `상품을 주문하면 포인트가 차감되고 주문이 생성된다`() {
        // given
        val memberId = 1L
        val product = Product(name = "커피", price = 3000, stock = 10, id = 1L)
        every { productService.getProduct(1L) } returns product
        every { pointService.deduct(memberId, 3000) } returns listOf(1L to 2000, 2L to 1000)
        every { orderRepository.save(any()) } returns
            Order(
                memberId = memberId,
                productId = 1L,
                usedPoint = 3000,
                id = 1L,
            )
        every { pointUsageRepository.save(any<PointUsage>()) } answers { firstArg() }

        // when
        val result = orderService.placeOrder(memberId, OrderRequest(productId = 1L))

        // then
        assertEquals(3000, result.usedPoint)
        assertEquals("커피", result.productName)
        assertEquals("COMPLETED", result.status)
        assertEquals(9, product.stock) // 재고 차감
    }

    @Test
    fun `비활성 상품 주문 시 BusinessException을 던진다`() {
        // given
        val product = Product(name = "커피", price = 3000, stock = 10, isActive = false, id = 1L)
        every { productService.getProduct(1L) } returns product

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                orderService.placeOrder(1L, OrderRequest(productId = 1L))
            }
        assertEquals(ErrorCode.PRODUCT_NOT_ACTIVE, exception.errorCode)
    }

    @Test
    fun `재고 없는 상품 주문 시 BusinessException을 던진다`() {
        // given
        val product = Product(name = "커피", price = 3000, stock = 0, id = 1L)
        every { productService.getProduct(1L) } returns product

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                orderService.placeOrder(1L, OrderRequest(productId = 1L))
            }
        assertEquals(ErrorCode.PRODUCT_OUT_OF_STOCK, exception.errorCode)
    }

    @Test
    fun `주문 취소 시 포인트가 환불되고 재고가 복원된다`() {
        // given
        val order = Order(memberId = 1L, productId = 1L, usedPoint = 3000, id = 1L)
        val product = Product(name = "커피", price = 3000, stock = 9, id = 1L)
        val usages = listOf(PointUsage(pointId = 10L, orderId = 1L, amount = 3000, id = 1L))

        every { orderRepository.findById(1L) } returns Optional.of(order)
        every { pointUsageRepository.findByOrderId(1L) } returns usages
        every { productService.getProduct(1L) } returns product

        // when
        val result = orderService.cancelOrder(1L)

        // then
        assertEquals("CANCELLED", result.status)
        assertEquals(10, product.stock) // 재고 복원
        verify { pointService.restore(listOf(10L to 3000)) }
    }

    @Test
    fun `이미 취소된 주문을 다시 취소하면 BusinessException을 던진다`() {
        // given
        val order = Order(memberId = 1L, productId = 1L, usedPoint = 3000, id = 1L)
        order.cancel()
        every { orderRepository.findById(1L) } returns Optional.of(order)

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                orderService.cancelOrder(1L)
            }
        assertEquals(ErrorCode.ORDER_ALREADY_CANCELLED, exception.errorCode)
    }

    @Test
    fun `존재하지 않는 주문을 취소하면 ORDER_NOT_FOUND 예외를 던진다`() {
        // given
        every { orderRepository.findById(999L) } returns Optional.empty()

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                orderService.cancelOrder(999L)
            }
        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `내 주문 내역을 조회할 수 있다`() {
        // given
        val memberId = 1L
        val now = LocalDateTime.now()
        val orders =
            listOf(
                Order(memberId = memberId, productId = 1L, usedPoint = 3000, orderedAt = now, id = 1L),
                Order(memberId = memberId, productId = 2L, usedPoint = 1500, orderedAt = now.minusDays(1), id = 2L),
            )
        val product1 = Product(name = "커피", price = 3000, stock = 8, id = 1L)
        val product2 = Product(name = "아이스크림", price = 1500, stock = 5, id = 2L)

        every { orderRepository.findByMemberIdOrderByOrderedAtDesc(memberId) } returns orders
        every { productService.getProduct(1L) } returns product1
        every { productService.getProduct(2L) } returns product2

        // when
        val result = orderService.getMyOrders(memberId)

        // then
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("커피", result[0].productName)
        assertEquals(3000, result[0].usedPoint)
        assertEquals("COMPLETED", result[0].status)
        assertEquals(2L, result[1].id)
        assertEquals("아이스크림", result[1].productName)
        assertEquals(1500, result[1].usedPoint)
    }

    @Test
    fun `전체 주문 내역을 조회할 수 있다`() {
        // given
        val now = LocalDateTime.now()
        val orders =
            listOf(
                Order(memberId = 1L, productId = 1L, usedPoint = 3000, orderedAt = now, id = 1L),
                Order(memberId = 2L, productId = 1L, usedPoint = 3000, orderedAt = now.minusDays(1), id = 2L),
            )
        val product = Product(name = "커피", price = 3000, stock = 8, id = 1L)

        every { orderRepository.findAllByOrderByOrderedAtDesc() } returns orders
        every { productService.getProduct(1L) } returns product

        // when
        val result = orderService.getAllOrders()

        // then
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("커피", result[0].productName)
        assertEquals(3000, result[0].usedPoint)
        assertEquals("COMPLETED", result[0].status)
        assertEquals(2L, result[1].id)
        assertEquals("커피", result[1].productName)
        assertEquals(3000, result[1].usedPoint)
    }
}
