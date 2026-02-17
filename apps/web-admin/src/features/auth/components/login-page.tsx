import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Input, Button, Typography, message } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import { login } from '../api/auth-api';
import { useAuthStore } from '../../../common/stores/auth-store';

const { Title } = Typography;

export function LoginPage(): React.ReactElement {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const authLogin = useAuthStore((s) => s.login);

  const handleSubmit = async (values: { nickname: string }): Promise<void> => {
    setLoading(true);
    try {
      const result = await login({ nickname: values.nickname });
      authLogin(result.accessToken, result.memberId, result.nickname, result.role);
      message.success(`${result.nickname}님 환영합니다.`);
      navigate('/');
    } catch {
      message.error('로그인에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
      <Card style={{ width: 400 }}>
        <Title level={3} style={{ textAlign: 'center', marginBottom: 32 }}>
          🎯 룰렛 어드민
        </Title>
        <Form onFinish={handleSubmit} size="large">
          <Form.Item name="nickname" rules={[{ required: true, message: '닉네임을 입력해주세요.' }]}>
            <Input prefix={<UserOutlined />} placeholder="관리자 닉네임" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              로그인
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
