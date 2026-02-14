import { useEffect, useState } from 'react';
import { Table, Tag, Button, Popconfirm, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { getAllOrders, cancelOrder, type Order } from '../api/order-api';
import dayjs from 'dayjs';

export function OrderPage(): React.ReactElement {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchOrders = async (): Promise<void> => {
    setLoading(true);
    try {
      const data = await getAllOrders();
      setOrders(data);
    } catch {
      message.error('주문 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleCancel = async (orderId: number): Promise<void> => {
    try {
      await cancelOrder(orderId);
      message.success('주문이 취소되었습니다. 포인트가 환불됩니다.');
      fetchOrders();
    } catch {
      message.error('주문 취소에 실패했습니다.');
    }
  };

  const columns: ColumnsType<Order> = [
    { title: '주문 ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '상품명', dataIndex: 'productName', key: 'productName' },
    { title: '사용 포인트', dataIndex: 'usedPoint', key: 'usedPoint', width: 120, render: (v: number) => `${v.toLocaleString()}P` },
    {
      title: '상태',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const color = status === 'COMPLETED' ? 'green' : 'red';
        const label = status === 'COMPLETED' ? '완료' : '취소';
        return <Tag color={color}>{label}</Tag>;
      },
    },
    {
      title: '주문일시',
      dataIndex: 'orderedAt',
      key: 'orderedAt',
      width: 180,
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '관리',
      key: 'action',
      width: 120,
      render: (_, record) =>
        record.status === 'COMPLETED' ? (
          <Popconfirm title="주문을 취소하시겠습니까?" description="사용된 포인트가 환불됩니다." onConfirm={() => handleCancel(record.id)} okText="취소하기" cancelText="닫기">
            <Button danger size="small">
              주문 취소
            </Button>
          </Popconfirm>
        ) : (
          <span style={{ color: '#999' }}>취소됨</span>
        ),
    },
  ];

  return (
    <>
      <h2>주문 내역</h2>
      <Table columns={columns} dataSource={orders} rowKey="id" loading={loading} style={{ marginTop: 16 }} size="middle" pagination={{ pageSize: 10 }} />
    </>
  );
}
