import React, { useCallback, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "react-toastify";
import { PageContainer, PageHeader, Subtitle, Title } from "../../styles/GlobalStyles";
import { useAssignors } from "../../hooks/useAssignors";
import { addDays, toIsoDate } from "../../utils/format";
import { isPositiveDecimal, normalizeDecimalInput } from "../../utils/money";
import { ReceivableForm } from "./components/ReceivableForm";
import { SimulationSummary } from "./components/SimulationSummary";
import { LastSettlementCard } from "./components/LastSettlementCard";
import { useLiveSimulation } from "./hooks/useLiveSimulation";
import { useIdempotencyKey } from "./hooks/useIdempotencyKey";
import { settle } from "./service";
import type {
  ReceivableFormState,
  ReceivableFormValidation,
  SettlementRequest,
  SettlementResponse,
  SimulationRequest,
} from "./types";
import { PanelGrid, SideColumn } from "./style";

const INITIAL_FORM_STATE: ReceivableFormState = {
  assignorId: "",
  documentNumber: "",
  receivableType: "TRADE_BILL",
  faceValue: "",
  dueDate: "",
  paymentCurrency: "BRL",
};

export const OperatorPanel: React.FC = () => {
  const { t } = useTranslation();
  const { assignors, isLoadingAssignors } = useAssignors("operatorPanel.service.assignorsError");
  const { resolveIdempotencyKey, finishAttempt } = useIdempotencyKey();

  const [formState, setFormState] = useState<ReceivableFormState>(INITIAL_FORM_STATE);
  const [isSettling, setIsSettling] = useState(false);
  const [lastSettlement, setLastSettlement] = useState<SettlementResponse | null>(null);

  const minimumDueDate = useMemo(() => toIsoDate(addDays(new Date(), 1)), []);
  const normalizedFaceValue = useMemo(() => normalizeDecimalInput(formState.faceValue), [formState.faceValue]);

  const validation = useMemo<ReceivableFormValidation>(
    () => ({
      assignorOk: formState.assignorId.length > 0,
      documentNumberOk: formState.documentNumber.trim().length > 0,
      faceValueOk: isPositiveDecimal(normalizedFaceValue),
      dueDateOk: formState.dueDate.length > 0 && formState.dueDate >= minimumDueDate,
    }),
    [formState.assignorId, formState.documentNumber, formState.dueDate, normalizedFaceValue, minimumDueDate],
  );

  const simulationRequest = useMemo<SimulationRequest | null>(() => {
    if (!validation.faceValueOk || !validation.dueDateOk || normalizedFaceValue === null) {
      return null;
    }
    return {
      receivableType: formState.receivableType,
      faceValue: Number(normalizedFaceValue),
      dueDate: formState.dueDate,
      paymentCurrency: formState.paymentCurrency,
    };
  }, [
    validation.faceValueOk,
    validation.dueDateOk,
    normalizedFaceValue,
    formState.receivableType,
    formState.dueDate,
    formState.paymentCurrency,
  ]);

  const simulation = useLiveSimulation(simulationRequest);

  const isFormValid = useMemo(
    () => validation.assignorOk && validation.documentNumberOk && validation.faceValueOk && validation.dueDateOk,
    [validation],
  );

  const handleFieldChange = useCallback(
    <K extends keyof ReceivableFormState>(field: K, value: ReceivableFormState[K]) => {
      setFormState((previousState) => ({ ...previousState, [field]: value }));
    },
    [],
  );

  const handleSettle = useCallback(async () => {
    if (!isFormValid || !simulationRequest) {
      toast.error(t("operatorPanel.validation.formInvalid"));
      return;
    }

    const settlementRequest: SettlementRequest = {
      ...simulationRequest,
      assignorId: Number(formState.assignorId),
      documentNumber: formState.documentNumber.trim(),
    };

    setIsSettling(true);
    try {
      const idempotencyKey = resolveIdempotencyKey(JSON.stringify(settlementRequest));
      const settlement = await settle(idempotencyKey, settlementRequest);
      if (!settlement) {
        return;
      }
      finishAttempt();
      setLastSettlement(settlement);
      setFormState((previousState) => ({ ...previousState, documentNumber: "" }));
    } finally {
      setIsSettling(false);
    }
  }, [
    isFormValid,
    simulationRequest,
    formState.assignorId,
    formState.documentNumber,
    resolveIdempotencyKey,
    finishAttempt,
    t,
  ]);

  return (
    <PageContainer>
      <PageHeader>
        <Title>{t("operatorPanel.title")}</Title>
        <Subtitle>{t("operatorPanel.subtitle")}</Subtitle>
      </PageHeader>
      <PanelGrid>
        <ReceivableForm
          formState={formState}
          validation={validation}
          assignors={assignors}
          isLoadingAssignors={isLoadingAssignors}
          minimumDueDate={minimumDueDate}
          onFieldChange={handleFieldChange}
        />
        <SideColumn>
          <SimulationSummary
            result={simulation.result}
            isLoading={simulation.isLoading}
            errorMessage={simulation.errorMessage}
            canSettle={isFormValid && simulation.result !== null && !simulation.isLoading}
            isSettling={isSettling}
            onSettle={handleSettle}
          />
          {lastSettlement && <LastSettlementCard settlement={lastSettlement} />}
        </SideColumn>
      </PanelGrid>
    </PageContainer>
  );
};
