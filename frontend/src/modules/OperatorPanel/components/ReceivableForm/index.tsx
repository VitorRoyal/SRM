import React from "react";
import { useTranslation } from "react-i18next";
import {
  Card,
  Field,
  FieldError,
  Input,
  Label,
  Row,
  SectionTitle,
  Select,
} from "../../../../styles/GlobalStyles";
import { CURRENCY_CODES, RECEIVABLE_TYPES } from "../../../../types/Receivable";
import type { Assignor } from "../../../../types/Assignor";
import type { ReceivableFormState, ReceivableFormValidation } from "../../types";
import { FormGrid } from "./style";

interface ReceivableFormProps {
  formState: ReceivableFormState;
  validation: ReceivableFormValidation;
  assignors: Assignor[];
  isLoadingAssignors: boolean;
  minimumDueDate: string;
  onFieldChange: <K extends keyof ReceivableFormState>(field: K, value: ReceivableFormState[K]) => void;
}

export const ReceivableForm: React.FC<ReceivableFormProps> = ({
  formState,
  validation,
  assignors,
  isLoadingAssignors,
  minimumDueDate,
  onFieldChange,
}) => {
  const { t } = useTranslation();

  const showFaceValueError = formState.faceValue.trim().length > 0 && !validation.faceValueOk;
  const showDueDateError = formState.dueDate.length > 0 && !validation.dueDateOk;

  const handleReceivableTypeChange = (event: React.ChangeEvent<HTMLSelectElement>): void => {
    const receivableType = RECEIVABLE_TYPES.find((type) => type === event.target.value);
    if (receivableType) {
      onFieldChange("receivableType", receivableType);
    }
  };

  const handlePaymentCurrencyChange = (event: React.ChangeEvent<HTMLSelectElement>): void => {
    const paymentCurrency = CURRENCY_CODES.find((currency) => currency === event.target.value);
    if (paymentCurrency) {
      onFieldChange("paymentCurrency", paymentCurrency);
    }
  };

  const assignorPlaceholder = () => {
    if (isLoadingAssignors) {
      return t("operatorPanel.form.loadingAssignors");
    }
    return t("operatorPanel.form.assignorPlaceholder");
  };

  return (
    <Card>
      <SectionTitle>{t("operatorPanel.form.sectionTitle")}</SectionTitle>
      <FormGrid>
        <Row>
          <Field>
            <Label>{t("operatorPanel.form.assignor")}</Label>
            <Select
              value={formState.assignorId}
              disabled={isLoadingAssignors}
              onChange={(event) => onFieldChange("assignorId", event.target.value)}
            >
              <option value="">{assignorPlaceholder()}</option>
              {assignors.map((assignor) => (
                <option key={assignor.id} value={String(assignor.id)}>
                  {assignor.name}
                </option>
              ))}
            </Select>
          </Field>
          <Field>
            <Label>{t("operatorPanel.form.documentNumber")}</Label>
            <Input
              value={formState.documentNumber}
              maxLength={50}
              placeholder={t("operatorPanel.form.documentNumberPlaceholder")}
              onChange={(event) => onFieldChange("documentNumber", event.target.value)}
            />
          </Field>
        </Row>
        <Row>
          <Field>
            <Label>{t("operatorPanel.form.receivableType")}</Label>
            <Select value={formState.receivableType} onChange={handleReceivableTypeChange}>
              {RECEIVABLE_TYPES.map((receivableType) => (
                <option key={receivableType} value={receivableType}>
                  {t(`common.receivableTypes.${receivableType}`)}
                </option>
              ))}
            </Select>
          </Field>
          <Field>
            <Label>{t("operatorPanel.form.paymentCurrency")}</Label>
            <Select value={formState.paymentCurrency} onChange={handlePaymentCurrencyChange}>
              {CURRENCY_CODES.map((currency) => (
                <option key={currency} value={currency}>
                  {t(`common.currencies.${currency}`)}
                </option>
              ))}
            </Select>
          </Field>
        </Row>
        <Row>
          <Field>
            <Label>{t("operatorPanel.form.faceValue")}</Label>
            <Input
              value={formState.faceValue}
              inputMode="decimal"
              placeholder={t("operatorPanel.form.faceValuePlaceholder")}
              $invalid={showFaceValueError}
              onChange={(event) => onFieldChange("faceValue", event.target.value)}
            />
            {showFaceValueError && <FieldError>{t("operatorPanel.validation.faceValueInvalid")}</FieldError>}
          </Field>
          <Field>
            <Label>{t("operatorPanel.form.dueDate")}</Label>
            <Input
              type="date"
              value={formState.dueDate}
              min={minimumDueDate}
              $invalid={showDueDateError}
              onChange={(event) => onFieldChange("dueDate", event.target.value)}
            />
            {showDueDateError && <FieldError>{t("operatorPanel.validation.dueDateInvalid")}</FieldError>}
          </Field>
        </Row>
      </FormGrid>
    </Card>
  );
};
