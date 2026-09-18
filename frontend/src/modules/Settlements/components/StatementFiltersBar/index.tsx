import React from "react";
import { useTranslation } from "react-i18next";
import { Card, Field, Input, Label, SecondaryButton, Select } from "../../../../styles/GlobalStyles";
import { CURRENCY_CODES } from "../../../../types/Receivable";
import type { Assignor } from "../../../../types/Assignor";
import type { StatementFilters } from "../../types";
import { FiltersGrid } from "./style";

interface StatementFiltersBarProps {
  filters: StatementFilters;
  assignors: Assignor[];
  isLoadingAssignors: boolean;
  onFiltersChange: (filters: StatementFilters) => void;
  onClear: () => void;
}

export const StatementFiltersBar: React.FC<StatementFiltersBarProps> = ({
  filters,
  assignors,
  isLoadingAssignors,
  onFiltersChange,
  onClear,
}) => {
  const { t } = useTranslation();

  const handlePaymentCurrencyChange = (event: React.ChangeEvent<HTMLSelectElement>): void => {
    const paymentCurrency = CURRENCY_CODES.find((currency) => currency === event.target.value);
    if (paymentCurrency) {
      onFiltersChange({ ...filters, paymentCurrency });
      return;
    }
    onFiltersChange({ ...filters, paymentCurrency: "" });
  };

  return (
    <Card>
      <FiltersGrid>
        <Field>
          <Label>{t("settlements.filters.settledFrom")}</Label>
          <Input
            type="date"
            value={filters.settledFrom}
            max={filters.settledTo || undefined}
            onChange={(event) => onFiltersChange({ ...filters, settledFrom: event.target.value })}
          />
        </Field>
        <Field>
          <Label>{t("settlements.filters.settledTo")}</Label>
          <Input
            type="date"
            value={filters.settledTo}
            min={filters.settledFrom || undefined}
            onChange={(event) => onFiltersChange({ ...filters, settledTo: event.target.value })}
          />
        </Field>
        <Field>
          <Label>{t("settlements.filters.assignor")}</Label>
          <Select
            value={filters.assignorId}
            disabled={isLoadingAssignors}
            onChange={(event) => onFiltersChange({ ...filters, assignorId: event.target.value })}
          >
            <option value="">{t("settlements.filters.allAssignors")}</option>
            {assignors.map((assignor) => (
              <option key={assignor.id} value={String(assignor.id)}>
                {assignor.name}
              </option>
            ))}
          </Select>
        </Field>
        <Field>
          <Label>{t("settlements.filters.paymentCurrency")}</Label>
          <Select value={filters.paymentCurrency} onChange={handlePaymentCurrencyChange}>
            <option value="">{t("settlements.filters.allCurrencies")}</option>
            {CURRENCY_CODES.map((currency) => (
              <option key={currency} value={currency}>
                {t(`common.currencies.${currency}`)}
              </option>
            ))}
          </Select>
        </Field>
        <SecondaryButton type="button" onClick={onClear}>
          {t("settlements.filters.clear")}
        </SecondaryButton>
      </FiltersGrid>
    </Card>
  );
};
