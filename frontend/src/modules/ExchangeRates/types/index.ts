import type { CurrencyCode } from "../../../types/Receivable";

export interface ExchangeRate {
  id: number;
  currency: CurrencyCode;
  brlPerUnit: number;
  effectiveAt: string;
  createdAt: string;
}

export interface ExchangeRateRequest {
  currency: CurrencyCode;
  brlPerUnit: number;
}
