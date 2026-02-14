import { useEffect, useState } from 'react';
import { Card, Col, Row, Statistic, Spin, message } from 'antd';
import { DollarOutlined, TeamOutlined, GiftOutlined, PercentageOutlined } from '@ant-design/icons';
import { getTodayBudget, type BudgetResponse } from '../../budget/api/budget-api';

export function DashboardPage(): React.ReactElement {
  const [budget, setBudget] = useState<BudgetResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async (): Promise<void> => {
      try {
        const data = await getTodayBudget();
        setBudget(data);
      } catch {
        message.error('대시보드 데이터를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) {
    return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />;
  }

  const usageRate = budget ? Math.round((budget.usedBudget / budget.totalBudget) * 100) : 0;

  return (
    <>
      <h2>대시보드</h2>
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="오늘 총 예산"
              value={budget?.totalBudget ?? 0}
              prefix={<DollarOutlined />}
              suffix="P"
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="사용된 예산"
              value={budget?.usedBudget ?? 0}
              prefix={<GiftOutlined />}
              suffix="P"
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="잔여 예산"
              value={budget?.remainingBudget ?? 0}
              prefix={<TeamOutlined />}
              suffix="P"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="예산 소진율"
              value={usageRate}
              prefix={<PercentageOutlined />}
              suffix="%"
              valueStyle={{ color: usageRate >= 80 ? '#cf1322' : '#3f8600' }}
            />
          </Card>
        </Col>
      </Row>
    </>
  );
}
