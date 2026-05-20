---
description: "Convenções de frontend para Next.js 15 App Router — TypeScript strict, Tailwind CSS, shadcn/ui, server components"
applyTo: '**/app/**,**/components/**,**/*.tsx,**/*.ts'
---

# Especificação de Frontend — Next.js 15 + TypeScript

Este arquivo é ativado quando você trabalha em arquivos TypeScript ou TSX, ou em qualquer coisa sob `app/` ou `components/`. Ele reforça as convenções de frontend para o sistema modernizado.

## Resumo da Stack

| Camada | Tecnologia | Versão |
|-------|-----------|---------|
| Framework | Next.js (App Router) | 15 |
| Linguagem | TypeScript (strict mode) | 5+ |
| Estilo | Tailwind CSS | 3.4+ |
| Componentes | shadcn/ui | Latest |
| Estado (client) | Zustand | 4+ |
| Estado (server) | React Query / TanStack Query | 5+ |
| Testes | Vitest + Testing Library | Latest |

## Padrões do App Router

### Server Components (Padrão)

Todo componente é um Server Component, salvo marcação explícita em contrário. Server Components:

- Rodam no servidor, nunca enviam JS para o client
- Podem usar `await` diretamente em fetches de dados
- Não podem usar hooks, event handlers ou APIs do browser

```tsx
// app/payments/page.tsx — Server Component (default)
export default async function PaymentsPage() {
  const payments = await fetch('/api/payments').then(r => r.json());
  return <PaymentList payments={payments} />;
}
```

### Client Components

Adicione `'use client'` somente quando precisar de interatividade:

```tsx
'use client';

import { useState } from 'react';

export function PaymentFilter({ onFilter }: { onFilter: (term: string) => void }) {
  const [term, setTerm] = useState('');
  return (
    <input
      value={term}
      onChange={e => { setTerm(e.target.value); onFilter(e.target.value); }}
      placeholder="Filter payments..."
    />
  );
}
```

Regras:

- **Minimize a superfície de `'use client'`**: Empurre a interatividade para o menor componente possível. Uma página que busca dados deve ser um Server Component; somente o filtro/form interativo dentro dela deve ser um Client Component.
- **Nunca exponha secrets em client components**: API keys, tokens e URLs internas devem permanecer server-side.

### Server Actions para Mutations

Use server actions em vez de API route handlers para submissões de formulário:

```tsx
// app/payments/actions.ts
'use server';

export async function createPayment(formData: FormData) {
  const amount = formData.get('amount');
  // Valida e chama a API de backend
  const res = await fetch(`${process.env.API_URL}/payments`, {
    method: 'POST',
    body: JSON.stringify({ amount }),
    headers: { 'Content-Type': 'application/json' },
  });
  if (!res.ok) throw new Error('Payment creation failed');
}
```

## Convenções TypeScript

- **`strict: true`** em `tsconfig.json` — sem exceções, sem `// @ts-ignore`
- **Sem `any`**: Use `unknown` e refine com type guards
- **Somente named exports**: `export function PaymentCard()` — sem `export default`
- **Interface em vez de type** para object shapes que podem ser estendidos
- **Utility types**: Use `Pick`, `Omit`, `Partial` em vez de duplicar interfaces

```tsx
// Good: named export, typed props
export function PaymentCard({ payment }: { payment: PaymentDto }) {
  return <div>{payment.amount}</div>;
}

// Bad: default export, any type
export default function PaymentCard({ payment }: { payment: any }) { ... }
```

## Tailwind CSS + shadcn/ui

- Use classes utilitárias do Tailwind diretamente — sem arquivos CSS separados, a menos que seja absolutamente necessário
- Use componentes shadcn/ui para elementos padrão de UI (Button, Card, Table, Dialog etc.)
- Siga os tokens de design system definidos em `design-system/` para cores e espaçamento
- Responsivo por padrão: mobile-first com breakpoints `sm:`, `md:`, `lg:`

```tsx
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export function PaymentSummary({ total }: { total: number }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Payment Summary</CardTitle>
      </CardHeader>
      <CardContent>
        <p className="text-2xl font-bold">{total.toLocaleString('en-US', { style: 'currency', currency: 'BRL' })}</p>
      </CardContent>
    </Card>
  );
}
```

## Baseline de Acessibilidade

Toda página e componente deve cumprir estes mínimos:

- Todas as imagens têm texto `alt`
- Inputs de formulário têm elementos `<label>` associados
- Elementos interativos são navegáveis por teclado
- Cor não é o único meio de transmitir informação
- A página tem um único `<h1>`, e headings seguem ordem lógica

## Testes com Vitest

```tsx
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { PaymentCard } from './PaymentCard';

describe('PaymentCard', () => {
  it('should display the payment amount', () => {
    render(<PaymentCard payment={{ id: 1, amount: 100.50 }} />);
    expect(screen.getByText('100.5')).toBeInTheDocument();
  });
});
```

Nome de teste: `should_[expected behavior]_when_[condition]` ou `displays [what] when [condition]`.

## O Que NÃO Fazer

- **Sem `export default`** em arquivos de componentes — use named exports
- **Sem `any`** — use `unknown` e type guards
- **Sem cadeias `.then()`** — use `async/await`
- **Sem CSS modules ou styled-components** — use Tailwind
- **Sem data fetching client-side em Server Components** — faça fetch diretamente com `await`
- **Sem secrets em arquivos `'use client'`** — variáveis de ambiente que começam com `NEXT_PUBLIC_` são expostas ao browser
