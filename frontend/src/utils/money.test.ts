import { describe, expect, it } from "vitest";
import { isPositiveDecimal, normalizeDecimalInput, normalizeRateInput } from "./money";

describe("normalizeDecimalInput", () => {
  it.each([
    ["100.000,00", "100000.00"],
    ["100,000.00", "100000.00"],
    ["100.000", "100000"],
    ["100,000", "100000"],
    ["1.000,5", "1000.5"],
    ["25000", "25000"],
    [" 1 234,56 ", "1234.56"],
  ])("reads %s as %s", (rawValue, expectedValue) => {
    expect(normalizeDecimalInput(rawValue)).toBe(expectedValue);
  });

  it.each([["abc"], ["12,345,6789"], ["1.2.3,456"], [""], ["-10,00"]])("rejects %s", (rawValue) => {
    expect(normalizeDecimalInput(rawValue)).toBeNull();
  });

  it("rejects values with more than 13 integer digits", () => {
    expect(normalizeDecimalInput("9999999999999,99")).toBe("9999999999999.99");
    expect(normalizeDecimalInput("99999999999999,99")).toBeNull();
  });
});

describe("normalizeRateInput", () => {
  it.each([
    ["5,4321", "5.4321"],
    ["5.43210000", "5.43210000"],
  ])("reads %s as %s", (rawValue, expectedValue) => {
    expect(normalizeRateInput(rawValue)).toBe(expectedValue);
  });

  it.each([["5,123456789"], ["1.000,50"], ["abc"]])("rejects %s", (rawValue) => {
    expect(normalizeRateInput(rawValue)).toBeNull();
  });
});

describe("isPositiveDecimal", () => {
  it("accepts only values greater than zero", () => {
    expect(isPositiveDecimal("0.01")).toBe(true);
    expect(isPositiveDecimal("0")).toBe(false);
    expect(isPositiveDecimal("0.00")).toBe(false);
    expect(isPositiveDecimal(null)).toBe(false);
  });
});
