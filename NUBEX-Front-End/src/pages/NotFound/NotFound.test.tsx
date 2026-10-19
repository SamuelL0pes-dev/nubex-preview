import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, it, expect } from "vitest";
import NotFound from "./NotFound";

describe("NotFound", () => {
  it("mostra o titulo de pagina nao encontrada", () => {
    render(
      <MemoryRouter>
        <NotFound />
      </MemoryRouter>,
    );

    expect(
      screen.getByRole("heading", { name: /página não encontrada/i }),
    ).toBeInTheDocument();
  });
});
