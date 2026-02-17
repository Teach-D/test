import { useState } from 'react';
import { Table, Button, Popconfirm, Modal, Form, Input } from 'antd';
import { UserAddOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useAdminMembers, useCreateAdmin, useDemoteAdmin } from '../hooks/use-admin-members';
import { useAuthStore } from '../../../common/stores/auth-store';
import type { AdminMember } from '../api/admin-member-api';

/** 관리자 추가 폼 필드 */
interface AddAdminFormValues {
  nickname: string;
}

/** 관리자 계정 관리 페이지 */
export function AdminMemberPage(): React.ReactElement {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm<AddAdminFormValues>();

  const { memberId: currentMemberId } = useAuthStore();
  const { data: adminMembers = [], isLoading } = useAdminMembers();
  const createAdmin = useCreateAdmin();
  const demoteAdmin = useDemoteAdmin();

  const handleOpenModal = (): void => {
    form.resetFields();
    setIsModalOpen(true);
  };

  const handleCloseModal = (): void => {
    setIsModalOpen(false);
  };

  const handleCreateAdmin = async (): Promise<void> => {
    const values = await form.validateFields();
    await createAdmin.mutateAsync({ nickname: values.nickname });
    setIsModalOpen(false);
  };

  const handleDemote = (memberId: number): void => {
    demoteAdmin.mutate(memberId);
  };

  const columns: ColumnsType<AdminMember> = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '닉네임', dataIndex: 'nickname', key: 'nickname' },
    {
      title: '생성일',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '관리',
      key: 'action',
      width: 140,
      render: (_, record) => {
        const isSelf = record.id === currentMemberId;
        return (
          <Popconfirm
            title="관리자 권한을 제거하시겠습니까?"
            description="해당 계정은 더 이상 관리자 기능을 사용할 수 없습니다."
            onConfirm={() => handleDemote(record.id)}
            okText="제거"
            cancelText="취소"
            disabled={isSelf}
          >
            <Button danger size="small" disabled={isSelf}>
              {isSelf ? '본인 계정' : '권한 제거'}
            </Button>
          </Popconfirm>
        );
      },
    },
  ];

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>관리자 계정 관리</h2>
        <Button type="primary" icon={<UserAddOutlined />} onClick={handleOpenModal}>
          관리자 추가
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={adminMembers}
        rowKey="id"
        loading={isLoading}
        size="middle"
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title="관리자 계정 추가"
        open={isModalOpen}
        onOk={handleCreateAdmin}
        onCancel={handleCloseModal}
        confirmLoading={createAdmin.isPending}
        okText="추가"
        cancelText="취소"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="nickname"
            label="닉네임"
            rules={[{ required: true, message: '닉네임을 입력해주세요.' }]}
          >
            <Input placeholder="닉네임을 입력하세요" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}