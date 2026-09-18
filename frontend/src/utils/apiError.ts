import i18next from "i18next";

interface ApiErrorShape {
  response?: {
    status?: number;
    data?: {
      detail?: string;
      fieldErrors?: Record<string, string>;
    };
  };
}

export const extractApiErrorStatus = (error: unknown): number | undefined => {
  const apiError = error as ApiErrorShape;
  return apiError?.response?.status;
};

export const extractApiErrorMessage = (error: unknown, fallbackKey: string): string => {
  const apiError = error as ApiErrorShape;
  if (apiError?.response === undefined) {
    return i18next.t("common.errors.network");
  }

  const fieldErrors = apiError.response.data?.fieldErrors;
  if (fieldErrors !== undefined) {
    const firstFieldError = Object.entries(fieldErrors)[0];
    if (firstFieldError !== undefined) {
      return `${firstFieldError[0]}: ${firstFieldError[1]}`;
    }
  }

  const detail = apiError.response.data?.detail;
  if (detail !== undefined) {
    return detail;
  }
  return i18next.t(fallbackKey);
};
