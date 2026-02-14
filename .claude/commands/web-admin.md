---
skills:
  - prompt
  - coding
agents:
  - web-admin
  - improve
---

React 18 + TypeScript + Ant Design 관리자 웹 개발 커맨드.

## 활성화되는 구성

- **prompt 스킬**: 요청이 불명확하면 분석 후 질문하여 프롬프트를 개선한다
- **coding 스킬**: 코드 작성 원칙, 함수 규칙, 커밋 규칙, 테스트 규칙을 따른다
- **web-admin 에이전트**: React + Ant Design 어드민 페이지를 작성하고 테스트한다
- **improve 에이전트**: 작성된 코드를 분석하고 개선점을 제안한다

## 작업 흐름

1. 사용자 요청을 prompt 스킬로 분석하여 명확화한다
2. web-admin 에이전트가 coding 스킬 규칙에 따라 코드를 작성한다
3. 기능 완성 후 Vitest + React Testing Library 테스트를 작성한다
4. improve 에이전트가 코드를 검토하고 개선점을 제안한다
5. 커밋 규칙에 따라 git add + commit한다

$ARGUMENTS
