import { CREDIT_ENGINE_NETWORK } from "../../../services/urlsService";
import type { PageResponse } from "../../../types/Page";
import type { SettlementRow, StatementFilters } from "../types";

const creditEngineClient = CREDIT_ENGINE_NETWORK.client;

export const getStatement = async (
  filters: StatementFilters,
  page: number,
  size: number,
  signal: AbortSignal,
): Promise<PageResponse<SettlementRow>> => {
  const params: Record<string, string | number> = { page, size };
  if (filters.settledFrom) {
    params.settledFrom = filters.settledFrom;
  }
  if (filters.settledTo) {
    params.settledTo = filters.settledTo;
  }
  if (filters.assignorId) {
    params.assignorId = filters.assignorId;
  }
  if (filters.paymentCurrency) {
    params.paymentCurrency = filters.paymentCurrency;
  }

  const response = await creditEngineClient.get<PageResponse<SettlementRow>>("/settlements", {
    params,
    signal,
  });
  return response.data;
};
