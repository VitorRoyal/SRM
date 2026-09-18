import { CREDIT_ENGINE_NETWORK } from "./urlsService";
import type { Assignor } from "../types/Assignor";

const creditEngineClient = CREDIT_ENGINE_NETWORK.client;

export const getAssignors = async (): Promise<Assignor[]> => {
  const response = await creditEngineClient.get<Assignor[]>("/assignors");
  return response.data;
};
