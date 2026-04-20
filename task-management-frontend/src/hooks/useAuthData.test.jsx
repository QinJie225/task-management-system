import { describe, it, expect, vi } from "vitest";
import { renderHook } from "@testing-library/react";
import { useAuthData } from "./useAuthData";
import { AuthContext } from "react-oauth2-code-pkce";

const createWrapper = (mockContextValue) => {
  return function Wrapper({ children }) {
    return (
      <AuthContext.Provider value={mockContextValue}>
        {children}
      </AuthContext.Provider>
    );
  };
};

describe("useAuthData Custom Hook", () => {
  it("should return unauthenticated state when no token is present", () => {
    const mockContext = {
      token: null,
      tokenData: null,
      logOut: vi.fn(),
      error: null,
    };

    const { result } = renderHook(() => useAuthData(), {
      wrapper: createWrapper(mockContext),
    });

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.isAuthorized).toBe(false);
    expect(result.current.isAdmin).toBe(false);
    expect(result.current.name).toBe("User"); 
  });

  it("should correctly identify an ADMIN user", () => {
    const mockContext = {
      token: "mock-jwt-token",
      tokenData: {
        preferred_username: "admin_user",
        name: "Admin User",
        email: "admin@example.com",
        realm_access: { roles: ["ADMIN", "USER"] },
      },
      logOut: vi.fn(),
      error: null,
    };

    const { result } = renderHook(() => useAuthData(), {
      wrapper: createWrapper(mockContext),
    });

    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.isAdmin).toBe(true);
    expect(result.current.isUser).toBe(true);
    expect(result.current.isAuthorized).toBe(true);
    expect(result.current.name).toBe("Admin User");
  });

  it("should map authorization correctly for a standard USER", () => {
    const mockContext = {
      token: "mock-jwt-token",
      tokenData: {
        preferred_username: "standard_user",
        realm_access: { roles: ["USER"] }, // No ADMIN role
      },
      logOut: vi.fn(),
      error: null,
    };

    const { result } = renderHook(() => useAuthData(), {
      wrapper: createWrapper(mockContext),
    });

    expect(result.current.isAdmin).toBe(false);
    expect(result.current.isUser).toBe(true);
    expect(result.current.isAuthorized).toBe(true);
    expect(result.current.name).toBe("standard_user"); 
  });

  describe("canModify function logic", () => {
    it("should allow an admin to modify any task", () => {
      const mockContext = {
        token: "token",
        tokenData: { realm_access: { roles: ["ADMIN"] } },
      };

      const { result } = renderHook(() => useAuthData(), {
        wrapper: createWrapper(mockContext),
      });

      // Admin modifying someone else's task
      expect(result.current.canModify({ createdBy: "other_user" })).toBe(true);
    });

    it("should allow a standard user to modify their own task", () => {
      const mockContext = {
        token: "token",
        tokenData: {
          preferred_username: "john_doe",
          realm_access: { roles: ["USER"] },
        },
      };

      const { result } = renderHook(() => useAuthData(), {
        wrapper: createWrapper(mockContext),
      });

      expect(result.current.canModify({ createdBy: "john_doe" })).toBe(true);
    });

    it("should deny a standard user from modifying someone else's task", () => {
      const mockContext = {
        token: "token",
        tokenData: {
          preferred_username: "john_doe",
          realm_access: { roles: ["USER"] },
        },
      };

      const { result } = renderHook(() => useAuthData(), {
        wrapper: createWrapper(mockContext),
      });

      expect(result.current.canModify({ createdBy: "jane_smith" })).toBe(false);
    });
  });
});