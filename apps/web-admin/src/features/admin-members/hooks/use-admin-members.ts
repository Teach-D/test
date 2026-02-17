import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { message } from 'antd';
import { getAdminMembers, createAdmin, demoteAdmin } from '../api/admin-member-api';
import type { CreateAdminRequest } from '../api/admin-member-api';

/** 쿼리 키 상수 */
const ADMIN_MEMBERS_QUERY_KEY = ['adminMembers'] as const;

/** 관리자 목록 조회 훅 */
export function useAdminMembers() {
  return useQuery({
    queryKey: ADMIN_MEMBERS_QUERY_KEY,
    queryFn: getAdminMembers,
  });
}

/** 관리자 생성 mutation 훅 */
export function useCreateAdmin() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateAdminRequest) => createAdmin(data),
    onSuccess: () => {
      message.success('관리자 계정이 생성되었습니다.');
      queryClient.invalidateQueries({ queryKey: ADMIN_MEMBERS_QUERY_KEY });
    },
    onError: () => {
      message.error('관리자 계정 생성에 실패했습니다.');
    },
  });
}

/** 관리자 권한 제거 mutation 훅 */
export function useDemoteAdmin() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (memberId: number) => demoteAdmin(memberId),
    onSuccess: () => {
      message.success('관리자 권한이 제거되었습니다.');
      queryClient.invalidateQueries({ queryKey: ADMIN_MEMBERS_QUERY_KEY });
    },
    onError: () => {
      message.error('관리자 권한 제거에 실패했습니다.');
    },
  });
}