import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createElement } from 'react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { useAdminMembers, useCreateAdmin, useDemoteAdmin } from './use-admin-members';
import * as adminMemberApi from '../api/admin-member-api';
import type { AdminMember } from '../api/admin-member-api';

/** 테스트용 QueryClient 생성 (재시도 비활성화) */
function createTestQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
}

/** 테스트용 래퍼 컴포넌트 생성 */
function createWrapper(queryClient: QueryClient) {
  return ({ children }: { children: React.ReactNode }) =>
    createElement(QueryClientProvider, { client: queryClient }, children);
}

const MOCK_ADMIN_MEMBERS: AdminMember[] = [
  { id: 1, nickname: '어드민1', createdAt: '2026-01-01T00:00:00' },
  { id: 2, nickname: '어드민2', createdAt: '2026-01-02T00:00:00' },
];

describe('useAdminMembers', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = createTestQueryClient();
  });

  it('관리자 목록을 성공적으로 조회한다', async () => {
    vi.spyOn(adminMemberApi, 'getAdminMembers').mockResolvedValue(MOCK_ADMIN_MEMBERS);

    const { result } = renderHook(() => useAdminMembers(), {
      wrapper: createWrapper(queryClient),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(MOCK_ADMIN_MEMBERS);
  });

  it('API 호출 실패 시 에러 상태를 반환한다', async () => {
    vi.spyOn(adminMemberApi, 'getAdminMembers').mockRejectedValue(new Error('서버 오류'));

    const { result } = renderHook(() => useAdminMembers(), {
      wrapper: createWrapper(queryClient),
    });

    await waitFor(() => expect(result.current.isError).toBe(true));
  });
});

describe('useCreateAdmin', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = createTestQueryClient();
  });

  it('관리자 생성 성공 시 쿼리를 무효화한다', async () => {
    const newAdmin: AdminMember = { id: 3, nickname: '신규어드민', createdAt: '2026-02-01T00:00:00' };
    vi.spyOn(adminMemberApi, 'createAdmin').mockResolvedValue(newAdmin);

    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

    const { result } = renderHook(() => useCreateAdmin(), {
      wrapper: createWrapper(queryClient),
    });

    result.current.mutate({ nickname: '신규어드민' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ['adminMembers'] });
  });
});

describe('useDemoteAdmin', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = createTestQueryClient();
  });

  it('권한 제거 성공 시 쿼리를 무효화한다', async () => {
    vi.spyOn(adminMemberApi, 'demoteAdmin').mockResolvedValue(undefined);

    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

    const { result } = renderHook(() => useDemoteAdmin(), {
      wrapper: createWrapper(queryClient),
    });

    result.current.mutate(1);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ['adminMembers'] });
  });

  it('권한 제거 API를 올바른 ID로 호출한다', async () => {
    const demoteSpy = vi.spyOn(adminMemberApi, 'demoteAdmin').mockResolvedValue(undefined);

    const { result } = renderHook(() => useDemoteAdmin(), {
      wrapper: createWrapper(queryClient),
    });

    result.current.mutate(42);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(demoteSpy).toHaveBeenCalledWith(42);
  });
});