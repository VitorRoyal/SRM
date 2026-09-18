import { toast } from "react-toastify";
import i18next from "i18next";
import { CREDIT_ENGINE_NETWORK } from "../../../services/urlsService";
import { extractApiErrorMessage } from "../../../utils/apiError";
import type { CurrencyCode } from "../../../types/Receivable";
import type { ExchangeRate, ExchangeRateRequest } from "../types";

const creditEngineClient = CREDIT_ENGINE_NETWORK.client;

export const getCurrentRate = async (currency: CurrencyCode): Promise<ExchangeRate> => {
  const response = await creditEngineClient.get<ExchangeRate>("/exchange-rates/current", {
    params: { currency },
  });
  return response.data;
};

export const registerRate = async (payload: ExchangeRateRequest): Promise<ExchangeRate | null> => {
  try {
    const response = await creditEngineClient.post<ExchangeRate>("/exchange-rates", payload);
    toast.success(i18next.t("exchangeRates.service.registerSuccess"));
    return response.data;
  } catch (error: unknown) {
    toast.error(extractApiErrorMessage(error, "exchangeRates.service.registerError"));
    return null;
  }
};
