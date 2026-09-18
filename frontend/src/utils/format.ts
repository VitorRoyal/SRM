import type { CurrencyCode } from "../types/Receivable";
import { resolveIntlLocale } from "./locale";

export const formatMoney = (value: number, currency: CurrencyCode, language: string): string =>
  new Intl.NumberFormat(resolveIntlLocale(language), {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);

export const formatPercent = (fraction: number, language: string): string =>
  new Intl.NumberFormat(resolveIntlLocale(language), {
    style: "percent",
    minimumFractionDigits: 2,
    maximumFractionDigits: 4,
  }).format(fraction);

export const formatExchangeRate = (brlPerUnit: number, language: string): string =>
  new Intl.NumberFormat(resolveIntlLocale(language), {
    minimumFractionDigits: 4,
    maximumFractionDigits: 8,
  }).format(brlPerUnit);

export const formatIsoDate = (isoDate: string, language: string): string => {
  const [year, month, day] = isoDate.split("-").map(Number);
  return new Intl.DateTimeFormat(resolveIntlLocale(language), { timeZone: "UTC" }).format(
    new Date(Date.UTC(year, month - 1, day)),
  );
};

export const formatDateTime = (isoInstant: string, language: string): string =>
  new Intl.DateTimeFormat(resolveIntlLocale(language), {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(isoInstant));

export const toIsoDate = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

export const addDays = (date: Date, days: number): Date => {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
};
