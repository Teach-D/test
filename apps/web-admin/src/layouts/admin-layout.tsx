import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Button, theme } from 'antd';
import {
  DashboardOutlined,
  DollarOutlined,
  ShoppingOutlined,
  OrderedListOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '../common/stores/auth-store';

const { Header, Sider, Content } = Layout;

const MENU_ITEMS = [
  { key: '/', icon: <DashboardOutlined />, label: '대시보드' },
  { key: '/budget', icon: <DollarOutlined />, label: '예산 관리' },
  { key: '/products', icon: <ShoppingOutlined />, label: '상품 관리' },
  { key: '/orders', icon: <OrderedListOutlined />, label: '주문 내역' },
];

export function AdminLayout(): React.ReactElement {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { nickname, logout } = useAuthStore();
  const { token: themeToken } = theme.useToken();

  const handleLogout = (): void => {
    logout();
    navigate('/login');
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div
          style={{
            height: 32,
            margin: 16,
            color: '#fff',
            fontSize: collapsed ? 14 : 18,
            fontWeight: 'bold',
            textAlign: 'center',
            lineHeight: '32px',
            overflow: 'hidden',
            whiteSpace: 'nowrap',
          }}
        >
          {collapsed ? '🎯' : '🎯 룰렛 어드민'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={MENU_ITEMS}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            padding: '0 24px',
            background: themeToken.colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
          />
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <span>{nickname}</span>
            <Button type="text" icon={<LogoutOutlined />} onClick={handleLogout}>
              로그아웃
            </Button>
          </div>
        </Header>
        <Content style={{ margin: 24, padding: 24, background: themeToken.colorBgContainer, borderRadius: 8 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
