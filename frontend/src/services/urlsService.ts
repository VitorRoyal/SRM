import axios from "axios";

export const CREDIT_ENGINE_NETWORK = {
  client: axios.create({
    baseURL: "/api",
    timeout: 10000,
  }),
};
