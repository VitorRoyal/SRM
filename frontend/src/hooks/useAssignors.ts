import { useEffect, useState } from "react";
import { toast } from "react-toastify";
import { getAssignors } from "../services/coreAPI";
import { extractApiErrorMessage } from "../utils/apiError";
import type { Assignor } from "../types/Assignor";

interface UseAssignorsResult {
  assignors: Assignor[];
  isLoadingAssignors: boolean;
}

export const useAssignors = (errorMessageKey: string): UseAssignorsResult => {
  const [assignors, setAssignors] = useState<Assignor[]>([]);
  const [isLoadingAssignors, setIsLoadingAssignors] = useState(true);

  useEffect(() => {
    let isActive = true;

    const loadAssignors = async () => {
      try {
        const assignorList = await getAssignors();
        if (isActive) {
          setAssignors(assignorList);
        }
      } catch (error: unknown) {
        if (isActive) {
          toast.error(extractApiErrorMessage(error, errorMessageKey), { toastId: errorMessageKey });
        }
      } finally {
        if (isActive) {
          setIsLoadingAssignors(false);
        }
      }
    };

    loadAssignors();
    return () => {
      isActive = false;
    };
  }, [errorMessageKey]);

  return { assignors, isLoadingAssignors };
};
