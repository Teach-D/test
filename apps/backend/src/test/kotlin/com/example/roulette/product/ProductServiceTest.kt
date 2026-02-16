package com.example.roulette.product

import com.example.roulette.common.exception.BusinessException
import com.example.roulette.common.exception.ErrorCode
import com.example.roulette.order.OrderRepository
import com.example.roulette.product.dto.ProductCreateRequest
import com.example.roulette.product.dto.ProductUpdateRequest
import com.example.roulette.product.entity.Product
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class ProductServiceTest {
    private lateinit var productRepository: ProductRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var productService: ProductService

    @BeforeEach
    fun setUp() {
        productRepository = mockk()
        orderRepository = mockk()
        productService = ProductService(productRepository, orderRepository)
    }

    @Test
    fun `상품을 등록하면 정상적으로 저장된다`() {
        // given
        val request = ProductCreateRequest(name = "커피", description = "아메리카노", price = 3000, stock = 10)
        val saved = Product(name = "커피", description = "아메리카노", price = 3000, stock = 10, id = 1L)
        val slot = slot<Product>()
        every { productRepository.save(capture(slot)) } returns saved

        // when
        val result = productService.create(request)

        // then
        assertEquals("커피", result.name)
        assertEquals(3000, result.price)
        assertEquals(10, result.stock)
    }

    @Test
    fun `상품을 수정하면 변경된 필드만 업데이트된다`() {
        // given
        val product = Product(name = "커피", description = "아메리카노", price = 3000, stock = 10, id = 1L)
        every { productRepository.findById(1L) } returns Optional.of(product)

        // when
        val result = productService.update(1L, ProductUpdateRequest(price = 2500))

        // then
        assertEquals("커피", result.name) // 변경 안 됨
        assertEquals(2500, result.price) // 변경됨
    }

    @Test
    fun `존재하지 않는 상품을 수정하면 BusinessException을 던진다`() {
        // given
        every { productRepository.findById(999L) } returns Optional.empty()

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                productService.update(999L, ProductUpdateRequest(name = "없는상품"))
            }
        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `활성 상품만 사용자에게 노출된다`() {
        // given
        val products =
            listOf(
                Product(name = "커피", price = 3000, stock = 10, isActive = true, id = 1L),
            )
        every { productRepository.findByIsActiveTrueOrderByCreatedAtDesc() } returns products

        // when
        val result = productService.getActiveProducts()

        // then
        assertEquals(1, result.size)
        assertTrue(result.all { it.isActive })
    }

    @Test
    fun `주문 내역이 없는 상품은 정상적으로 삭제된다`() {
        // given
        val product = Product(name = "커피", price = 3000, stock = 10, id = 1L)
        every { productRepository.findById(1L) } returns Optional.of(product)
        every { orderRepository.existsByProductId(1L) } returns false
        justRun { productRepository.delete(product) }

        // when
        productService.delete(1L)

        // then
        verify { productRepository.delete(product) }
    }

    @Test
    fun `주문 내역이 있는 상품을 삭제하면 PRODUCT_HAS_ORDERS 예외를 던진다`() {
        // given
        val product = Product(name = "커피", price = 3000, stock = 10, id = 1L)
        every { productRepository.findById(1L) } returns Optional.of(product)
        every { orderRepository.existsByProductId(1L) } returns true

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                productService.delete(1L)
            }
        assertEquals(ErrorCode.PRODUCT_HAS_ORDERS, exception.errorCode)
    }

    @Test
    fun `존재하지 않는 상품을 삭제하면 PRODUCT_NOT_FOUND 예외를 던진다`() {
        // given
        every { productRepository.findById(999L) } returns Optional.empty()

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                productService.delete(999L)
            }
        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.errorCode)
    }
}
