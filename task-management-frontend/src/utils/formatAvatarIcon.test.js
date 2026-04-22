import { describe, it, expect } from "vitest";
import { formatAvatarIcon } from "./formatAvatarIcon";

describe("formatAvatarIcon", () => {
  it("should return the uppercase first letter of a valid string", () => {
    expect(formatAvatarIcon("john")).toBe("J");
    expect(formatAvatarIcon("Alice")).toBe("A");
  });

  it("should return '?' when the input is null or undefined", () => {
    expect(formatAvatarIcon(null)).toBe("?");
    expect(formatAvatarIcon(undefined)).toBe("?");
  });

  it("should return '?' when the input is an empty string", () => {
    expect(formatAvatarIcon("")).toBe("?");
  });

  it("should handle non-alphabetic starting characters", () => {
    expect(formatAvatarIcon("1admin")).toBe("1");
    expect(formatAvatarIcon("_user")).toBe("_");
  });
});