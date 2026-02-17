import client from '../../../common/api/client';
import type { ApiResponse } from '../../../common/types/api';

/** 관리자 계정 정보 */
export interface AdminMember {
  id: number;
  nickname: string;
  createdAt: string;
}

/** 관리자 생성 요청 */
export interface CreateAdminRequest {
  nickname: string;
}

/** 관리자 목록 조회 */
export async function getAdminMembers(): Promise<AdminMember[]> {
  const response = await client.get<ApiResponse<AdminMember[]>>('/api/admin/members');
  return response.data.data!;
}

/** 관리자 계정 생성 */
export async function createAdmin(data: CreateAdminRequest): Promise<AdminMember> {
  const response = await client.post<ApiResponse<AdminMember>>('/api/admin/members', data);
  return response.data.data!;
}

/** 관리자 권한 제거 */
export async function demoteAdmin(memberId: number): Promise<void> {
  await client.delete<ApiResponse<null>>(`/api/admin/members/${memberId}`);
}