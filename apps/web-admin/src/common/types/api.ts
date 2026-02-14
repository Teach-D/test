/** 백엔드 공통 응답 래퍼 */
export interface ApiResponse<T> {
  status: 'SUCCESS' | 'ERROR';
  data?: T;
  errorCode?: string;
  message?: string;
}
