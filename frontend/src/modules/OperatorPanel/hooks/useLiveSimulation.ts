import { useEffect, useState } from "react";
import { toast } from "react-toastify";
import { simulate } from "../service";
import { extractApiErrorMessage } from "../../../utils/apiError";
import type { SimulationRequest, SimulationResponse } from "../types";

const SIMULATION_DEBOUNCE_MS = 350;
const SIMULATION_TOAST_ID = "operator-panel-simulation";

interface LiveSimulationState {
  result: SimulationResponse | null;
  isLoading: boolean;
  errorMessage: string | null;
}

interface ResolvedSimulation {
  request: SimulationRequest | null;
  result: SimulationResponse | null;
  errorMessage: string | null;
}

const IDLE_STATE: LiveSimulationState = {
  result: null,
  isLoading: false,
  errorMessage: null,
};

export const useLiveSimulation = (request: SimulationRequest | null): LiveSimulationState => {
  const [resolvedSimulation, setResolvedSimulation] = useState<ResolvedSimulation>({
    request: null,
    result: null,
    errorMessage: null,
  });

  useEffect(() => {
    if (!request) {
      return;
    }

    const abortController = new AbortController();

    const debounceTimer = window.setTimeout(async () => {
      try {
        const result = await simulate(request, abortController.signal);
        setResolvedSimulation({ request, result, errorMessage: null });
        toast.dismiss(SIMULATION_TOAST_ID);
      } catch (error: unknown) {
        if (abortController.signal.aborted) {
          return;
        }
        const errorMessage = extractApiErrorMessage(error, "operatorPanel.service.simulationError");
        setResolvedSimulation({ request, result: null, errorMessage });
        toast.error(errorMessage, { toastId: SIMULATION_TOAST_ID });
      }
    }, SIMULATION_DEBOUNCE_MS);

    return () => {
      window.clearTimeout(debounceTimer);
      abortController.abort();
    };
  }, [request]);

  if (!request) {
    return IDLE_STATE;
  }

  return {
    result: resolvedSimulation.result,
    isLoading: resolvedSimulation.request !== request,
    errorMessage: resolvedSimulation.errorMessage,
  };
};
