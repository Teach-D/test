import { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Switch, Space, Tag, Popconfirm, message } from 'antd';
import { PlusOutlined, EditOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { getAllProducts, createProduct, updateProduct, type Product, type ProductCreateRequest } from '../api/product-api';

export function ProductPage(): React.ReactElement {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  const fetchProducts = async (): Promise<void> => {
    setLoading(true);
    try {
      const data = await getAllProducts();
      setProducts(data);
    } catch {
      message.error('상품 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const openCreateModal = (): void => {
    setEditingProduct(null);
    form.resetFields();
    form.setFieldsValue({ stock: 0, isActive: true });
    setModalOpen(true);
  };

  const openEditModal = (product: Product): void => {
    setEditingProduct(product);
    form.setFieldsValue(product);
    setModalOpen(true);
  };

  const handleSubmit = async (): Promise<void> => {
    try {
      const values = await form.validateFields();
      setSaving(true);

      if (editingProduct) {
        await updateProduct(editingProduct.id, values);
        message.success('상품이 수정되었습니다.');
      } else {
        await createProduct(values as ProductCreateRequest);
        message.success('상품이 등록되었습니다.');
      }

      setModalOpen(false);
      fetchProducts();
    } catch {
      message.error('저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleToggleActive = async (product: Product): Promise<void> => {
    try {
      await updateProduct(product.id, { isActive: !product.isActive });
      message.success(product.isActive ? '상품이 비활성화되었습니다.' : '상품이 활성화되었습니다.');
      fetchProducts();
    } catch {
      message.error('상태 변경에 실패했습니다.');
    }
  };

  const columns: ColumnsType<Product> = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: '상품명', dataIndex: 'name', key: 'name' },
    { title: '설명', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: '가격', dataIndex: 'price', key: 'price', width: 120, render: (v: number) => `${v.toLocaleString()}P` },
    { title: '재고', dataIndex: 'stock', key: 'stock', width: 80 },
    {
      title: '상태',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 100,
      render: (v: boolean) => (v ? <Tag color="green">판매중</Tag> : <Tag color="default">비활성</Tag>),
    },
    {
      title: '관리',
      key: 'action',
      width: 180,
      render: (_, record) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEditModal(record)}>
            수정
          </Button>
          <Popconfirm
            title={record.isActive ? '상품을 비활성화하시겠습니까?' : '상품을 활성화하시겠습니까?'}
            onConfirm={() => handleToggleActive(record)}
            okText="확인"
            cancelText="취소"
          >
            <Button size="small" danger={record.isActive}>
              {record.isActive ? '비활성화' : '활성화'}
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>상품 관리</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
          상품 등록
        </Button>
      </div>

      <Table columns={columns} dataSource={products} rowKey="id" loading={loading} style={{ marginTop: 16 }} size="middle" pagination={{ pageSize: 10 }} />

      <Modal
        title={editingProduct ? '상품 수정' : '상품 등록'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        confirmLoading={saving}
        okText="저장"
        cancelText="취소"
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="name" label="상품명" rules={[{ required: true, message: '상품명을 입력해주세요.' }]}>
            <Input placeholder="상품명" />
          </Form.Item>
          <Form.Item name="description" label="설명">
            <Input.TextArea rows={3} placeholder="상품 설명" />
          </Form.Item>
          <Form.Item name="price" label="가격 (P)" rules={[{ required: true, message: '가격을 입력해주세요.' }]}>
            <InputNumber min={1} style={{ width: '100%' }} placeholder="가격" />
          </Form.Item>
          <Form.Item name="stock" label="재고">
            <InputNumber min={0} style={{ width: '100%' }} placeholder="재고" />
          </Form.Item>
          {editingProduct && (
            <Form.Item name="isActive" label="판매 상태" valuePropName="checked">
              <Switch checkedChildren="판매중" unCheckedChildren="비활성" />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </>
  );
}
