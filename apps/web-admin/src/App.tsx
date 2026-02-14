import { RouterProvider } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import koKR from 'antd/locale/ko_KR';
import { router } from './router';

function App(): React.ReactElement {
  return (
    <ConfigProvider locale={koKR}>
      <RouterProvider router={router} />
    </ConfigProvider>
  );
}

export default App;
