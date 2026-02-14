import { type ReactElement, useEffect, useState } from 'react';
import { getProducts } from '../api/product-api';
import { placeOrder } from '@/features/order/api/order-api';
import { getPointBalance } from '@/features/point/api/point-api';
import type {
  ProductResponse,
  PointBalanceResponse,
} from '@/common/types/api';
import { LoadingSpinner } from '@/common/components/loading-spinner';
import { ErrorMessage } from '@/common/components/error-message';

export function ProductPage(): ReactElement {
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [balance, setBalance] = useState<PointBalanceResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedProduct, setSelectedProduct] = useState<ProductResponse | null>(
    null,
  );
  const [isPurchasing, setIsPurchasing] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // 초기 데이터 로드
  const loadData = async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError(null);
      const [productsData, balanceData] = await Promise.all([
        getProducts(),
        getPointBalance(),
      ]);
      setProducts(productsData);
      setBalance(balanceData);
    } catch (err) {
      const message = err instanceof Error ? err.message : '데이터를 불러오는 중 오류가 발생했습니다.';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadData();
  }, []);

  // 구매 확인 모달 열기
  const handlePurchaseClick = (product: ProductResponse): void => {
    setSelectedProduct(product);
    setSuccessMessage(null);
  };

  // 구매 취소
  const handleCancelPurchase = (): void => {
    setSelectedProduct(null);
  };

  // 구매 확정
  const handleConfirmPurchase = async (): Promise<void> => {
    if (!selectedProduct) return;

    try {
      setIsPurchasing(true);
      setError(null);
      await placeOrder({ productId: selectedProduct.id });
      setSuccessMessage(
        `'${selectedProduct.name}'을(를) 성공적으로 구매했습니다!`,
      );
      setSelectedProduct(null);
      // 상품 목록과 잔액 새로고침
      await loadData();
    } catch (err) {
      const message = err instanceof Error ? err.message : '구매 중 오류가 발생했습니다.';
      setError(message);
      setSelectedProduct(null);
    } finally {
      setIsPurchasing(false);
    }
  };

  // 구매 가능 여부 확인
  const canPurchase = (product: ProductResponse): boolean => {
    return (
      product.stock > 0 &&
      balance !== null &&
      balance.totalBalance >= product.price
    );
  };

  // 구매 버튼 텍스트
  const getPurchaseButtonText = (product: ProductResponse): string => {
    if (product.stock === 0) return '품절';
    if (balance !== null && balance.totalBalance < product.price)
      return '포인트 부족';
    return '구매하기';
  };

  if (isLoading) {
    return (
      <div className="flex h-full items-center justify-center p-4">
        <div className="text-center">
          <LoadingSpinner />
          <p className="mt-2 text-gray-600">로딩 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-full flex-col p-4">
      {/* 포인트 잔액 헤더 */}
      <div className="mb-6 rounded-lg bg-white p-4 shadow">
        <p className="text-sm text-gray-600">내 포인트</p>
        <p className="text-xl font-bold text-indigo-600">
          {balance?.totalBalance.toLocaleString() ?? 0} P
        </p>
      </div>

      {/* 성공 메시지 */}
      {successMessage && (
        <div className="mb-4 rounded-lg bg-green-50 p-4 text-sm text-green-800">
          {successMessage}
        </div>
      )}

      {/* 에러 메시지 */}
      {error && (
        <div className="mb-4">
          <ErrorMessage message={error} />
        </div>
      )}

      {/* 상품 목록 */}
      {products.length === 0 ? (
        <div className="flex flex-1 items-center justify-center">
          <p className="text-gray-500">등록된 상품이 없습니다.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {products.map((product) => (
            <div
              key={product.id}
              className="flex flex-col rounded-xl bg-white p-4 shadow-sm"
            >
              <h3 className="mb-2 text-lg font-bold text-gray-900">
                {product.name}
              </h3>
              {product.description && (
                <p className="mb-3 line-clamp-2 text-sm text-gray-600">
                  {product.description}
                </p>
              )}
              <div className="mb-4 flex-1">
                <p className="mb-1 text-lg font-bold text-indigo-600">
                  {product.price.toLocaleString()} P
                </p>
                <p className="text-sm text-gray-500">
                  재고 {product.stock}개
                </p>
              </div>
              <button
                onClick={() => handlePurchaseClick(product)}
                disabled={!canPurchase(product)}
                className="w-full rounded-lg bg-indigo-600 py-2 text-sm font-semibold text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-gray-400"
              >
                {getPurchaseButtonText(product)}
              </button>
            </div>
          ))}
        </div>
      )}

      {/* 구매 확인 모달 */}
      {selectedProduct && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
          <div className="w-full max-w-sm rounded-lg bg-white p-6 shadow-xl">
            <h3 className="mb-4 text-xl font-bold text-gray-900">
              구매 확인
            </h3>
            <p className="mb-6 text-gray-700">
              <span className="font-semibold">'{selectedProduct.name}'</span>
              을(를){' '}
              <span className="font-semibold text-indigo-600">
                {selectedProduct.price.toLocaleString()}P
              </span>
              로 구매하시겠습니까?
            </p>
            <div className="flex gap-3">
              <button
                onClick={handleCancelPurchase}
                disabled={isPurchasing}
                className="flex-1 rounded-lg border border-gray-300 bg-white px-4 py-2 font-semibold text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
              >
                취소
              </button>
              <button
                onClick={handleConfirmPurchase}
                disabled={isPurchasing}
                className="flex-1 rounded-lg bg-indigo-600 px-4 py-2 font-semibold text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {isPurchasing ? '처리 중...' : '구매'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
