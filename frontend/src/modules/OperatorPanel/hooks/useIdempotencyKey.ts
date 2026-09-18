import { useCallback, useRef } from "react";

interface SettlementAttempt {
  requestSignature: string;
  idempotencyKey: string;
}

export const useIdempotencyKey = () => {
  const currentAttemptRef = useRef<SettlementAttempt | null>(null);

  const resolveIdempotencyKey = useCallback((requestSignature: string): string => {
    const currentAttempt = currentAttemptRef.current;
    if (currentAttempt && currentAttempt.requestSignature === requestSignature) {
      return currentAttempt.idempotencyKey;
    }
    const idempotencyKey = crypto.randomUUID();
    currentAttemptRef.current = { requestSignature, idempotencyKey };
    return idempotencyKey;
  }, []);

  const finishAttempt = useCallback(() => {
    currentAttemptRef.current = null;
  }, []);

  return { resolveIdempotencyKey, finishAttempt };
};
