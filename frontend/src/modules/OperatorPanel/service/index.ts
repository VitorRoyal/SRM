import { toast } from "react-toastify";
import i18next from "i18next";
import { CREDIT_ENGINE_NETWORK } from "../../../services/urlsService";
import { extractApiErrorMessage } from "../../../utils/apiError";
import type {
  SettlementRequest,
  SettlementResponse,
  SimulationRequest,
  SimulationResponse,
} from "../types";

const creditEngineClient = CREDIT_ENGINE_NETWORK.client;

export const simulate = async (
  payload: SimulationRequest,
  signal: AbortSignal,
): Promise<SimulationResponse> => {
  const response = await creditEngineClient.post<SimulationResponse>("/pricing/simulations", payload, {
    signal,
  });
  return response.data;
};

export const settle = async (
  idempotencyKey: string,
  payload: SettlementRequest,
): Promise<SettlementResponse | null> => {
  try {
    const response = await creditEngineClient.post<SettlementResponse>("/settlements", payload, {
      headers: { "Idempotency-Key": idempotencyKey },
    });
    if (response.headers["idempotent-replayed"] === "true") {
      toast.info(i18next.t("operatorPanel.service.settleReplayed"));
    } else {
      toast.success(i18next.t("operatorPanel.service.settleSuccess"));
    }
    return response.data;
  } catch (error: unknown) {
    toast.error(extractApiErrorMessage(error, "operatorPanel.service.settleError"));
    return null;
  }
};
