import { useEffect, useState } from 'react';
import { Card, InputNumber, Button, Table, Tag, Space, Popconfirm, message, Statistic, Row, Col } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { getTodayBudget, setTodayBudget, cancelRoulette, type BudgetResponse, type RouletteHistory } from '../api/budget-api';
import client from '../../../common/api/client';
import type { ApiResponse } from '../../../common/types/api';

export function BudgetPage(): React.ReactElement {
  const [budget, setBudget] = useState<BudgetResponse | null>(null);
  const [newBudget, setNewBudget] = useState<number>(100000);
  const [rouletteList, setRouletteList] = useState<RouletteHistory[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const fetchData = async (): Promise<void> => {
    setLoading(true);
    try {
      const budgetData = await getTodayBudget();
      setBudget(budgetData);
      setNewBudget(budgetData.totalBudget);

      // 룰렛 참여 내역 조회
      const rouletteResponse = await client.get<ApiResponse<RouletteHistory[]>>('/api/admin/roulette/histories');
      setRouletteList(rouletteResponse.data.data ?? []);
    } catch {
      message.error('데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleSaveBudget = async (): Promise<void> => {
    setSaving(true);
    try {
      const updated = await setTodayBudget(newBudget);
      setBudget(updated);
      message.success('예산이 설정되었습니다.');
    } catch {
      message.error('예산 설정에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleCancelRoulette = async (historyId: number): Promise<void> => {
    try {
      await cancelRoulette(historyId);
      message.success('룰렛 참여가 취소되었습니다.');
      fetchData();
    } catch {
      message.error('룰렛 취소에 실패했습니다.');
    }
  };

  const columns: ColumnsType<RouletteHistory> = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '회원 ID', dataIndex: 'memberId', key: 'memberId', width: 100 },
    { title: '지급 포인트', dataIndex: 'point', key: 'point', width: 120, render: (v: number) => `${v.toLocaleString()}P` },
    { title: '참여일', dataIndex: 'playedAt', key: 'playedAt', width: 120 },
    {
      title: '상태',
      dataIndex: 'isCancelled',
      key: 'isCancelled',
      width: 100,
      render: (v: boolean) => (v ? <Tag color="red">취소됨</Tag> : <Tag color="green">정상</Tag>),
    },
    {
      title: '관리',
      key: 'action',
      width: 120,
      render: (_, record) =>
        !record.isCancelled ? (
          <Popconfirm title="이 룰렛 참여를 취소하시겠습니까?" description="지급된 포인트가 회수됩니다." onConfirm={() => handleCancelRoulette(record.id)} okText="취소하기" cancelText="닫기">
            <Button danger size="small">
              참여 취소
            </Button>
          </Popconfirm>
        ) : (
          <span style={{ color: '#999' }}>취소됨</span>
        ),
    },
  ];

  return (
    <>
      <h2>예산 관리</h2>

      {/* 예산 현황 */}
      <Card title="오늘 예산 현황" style={{ marginTop: 16, marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col span={6}>
            <Statistic title="총 예산" value={budget?.totalBudget ?? 0} suffix="P" />
          </Col>
          <Col span={6}>
            <Statistic title="사용 예산" value={budget?.usedBudget ?? 0} suffix="P" valueStyle={{ color: '#cf1322' }} />
          </Col>
          <Col span={6}>
            <Statistic title="잔여 예산" value={budget?.remainingBudget ?? 0} suffix="P" valueStyle={{ color: '#3f8600' }} />
          </Col>
          <Col span={6}>
            <Space>
              <InputNumber min={0} step={10000} value={newBudget} onChange={(v) => setNewBudget(v ?? 0)} style={{ width: 150 }} />
              <Button type="primary" loading={saving} onClick={handleSaveBudget}>
                예산 설정
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 룰렛 참여 내역 */}
      <Card title="룰렛 참여 내역">
        <Table columns={columns} dataSource={rouletteList} rowKey="id" loading={loading} size="middle" pagination={{ pageSize: 10 }} />
      </Card>
    </>
  );
}
