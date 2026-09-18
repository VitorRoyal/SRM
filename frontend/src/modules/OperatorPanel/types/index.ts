import type { CurrencyCode, ReceivableType } from "../../../types/Receivable";

export interface SimulationRequest {
  receivableType: ReceivableType;
  faceValue: number;
  dueDate: string;
  paymentCurrency: CurrencyCode;
}

export interface SimulationResponse {
  termInMonths: number;
  monthlyBaseRate: number;
  monthlySpread: number;
  presentValueBrl: number;
  discountBrl: number;
  paymentCurrency: CurrencyCode;
  paymentAmount: number;
  exchangeRateBrlPerUnit: number | null;
  exchangeRateEffectiveAt: string | null;
}

export interface SettlementRequest extends SimulationRequest {
  assignorId: number;
  documentNumber: string;
}

export interface SettlementResponse {
  id: number;
  assignorId: number;
  assignorName: string;
  documentNumber: string;
  receivableType: ReceivableType;
  faceValue: number;
  dueDate: string;
  termInMonths: number;
  presentValueBrl: number;
  discountBrl: number;
  paymentCurrency: CurrencyCode;
  paymentAmount: number;
  exchangeRateBrlPerUnit: number | null;
  settledAt: string;
}

export interface ReceivableFormState {
  assignorId: string;
  documentNumber: string;
  receivableType: ReceivableType;
  faceValue: string;
  dueDate: string;
  paymentCurrency: CurrencyCode;
}

export interface ReceivableFormValidation {
  assignorOk: boolean;
  documentNumberOk: boolean;
  faceValueOk: boolean;
  dueDateOk: boolean;
}
