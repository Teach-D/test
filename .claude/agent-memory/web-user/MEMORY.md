# Web User App - Agent Memory

## Project Structure
- Feature-based organization: `src/features/<feature>/components|api|hooks`
- Common utilities: `src/common/components|api|types`
- Router configuration: `src/router.tsx`

## Established Patterns

### Component Structure
- Use functional components with TypeScript
- State management with `useState` and `useEffect`
- Loading/Error states with LoadingSpinner and ErrorMessage components
- Empty state handling with descriptive messages

### API Integration
- API calls in dedicated `api/` directory per feature
- Type-safe responses using `ApiResponse<T>` wrapper
- Error handling with try/catch and console.warn for errors
- Refresh functionality exposed to users

### Date Formatting
- Use `dayjs` library for all date formatting
- Standard format: `YYYY.MM.DD HH:mm`

### Styling
- Tailwind CSS for all styling
- Status badges: `rounded-full px-2 py-0.5 text-xs font-medium`
- Cards: `rounded-xl bg-white p-4 shadow-sm`
- Mobile-first responsive design

### File Naming
- kebab-case for files: `order-page.tsx`, `order-api.ts`
- PascalCase for components: `OrderPage`

## Completed Features
- Login page with JWT authentication
- Roulette page with daily participation
- Point balance and history page
- Product listing and purchase page
- Order history page
