import { act, cleanup, fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter, useNavigate } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import AnimatedCounters from "./components/features/AnimatedCounters/AnimatedCounters";
import Header from "./components/layout/Header/Header";
import Parceiros from "./pages/Parceiros/Parceiros";
import TextoDigitado from "./components/ui/TextoDigitado/TextoDigitado";

vi.mock("./mocks/usuario", () => ({ USUARIO_ATUAL: { nome: "Teste" } }));
vi.mock("./components/features/Partners/PartnersList", () => ({
  default: ({ destacado }: { destacado: string }) => (
    <div id="parceiro-teste" data-testid="partners" data-highlight={destacado} />
  ),
}));

afterEach(() => {
  cleanup();
  vi.useRealTimers();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

function Navigate({ to }: { to: string }) {
  const navigate = useNavigate();
  return <button onClick={() => navigate(to)}>Navegar</button>;
}

describe("comportamentos preservados após correção dos efeitos", () => {
  it("mostra os valores finais sem iniciar animação quando movimento reduzido está ativo", () => {
    vi.stubGlobal("matchMedia", vi.fn(() => ({
      matches: true,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
    })));
    let onVisible: () => void = () => {};
    vi.stubGlobal("IntersectionObserver", class {
      constructor(callback: (entries: { isIntersecting: boolean }[]) => void) {
        onVisible = () => callback([{ isIntersecting: true }]);
      }
      observe() {}
      disconnect() {}
    });
    const animate = vi.fn();
    vi.stubGlobal("requestAnimationFrame", animate);
    render(<AnimatedCounters />);
    act(() => onVisible());
    expect(screen.getByText("+4.000")).toBeInTheDocument();
    expect(screen.getByText("+30")).toBeInTheDocument();
    expect(animate).not.toHaveBeenCalled();
  });

  it("fecha o menu e a conta ao trocar de página", () => {
    render(<MemoryRouter><Header /><Navigate to="/quem-somos" /></MemoryRouter>);
    fireEvent.click(screen.getByRole("button", { name: /sobre/i }));
    fireEvent.click(screen.getByRole("button", { name: /teste/i }));
    expect(screen.getByRole("button", { name: /sobre/i })).toHaveAttribute("aria-expanded", "true");
    expect(screen.getByRole("button", { name: /teste/i })).toHaveAttribute("aria-expanded", "true");
    fireEvent.click(screen.getByRole("button", { name: "Navegar" }));
    expect(screen.getByRole("button", { name: /sobre/i })).toHaveAttribute("aria-expanded", "false");
    expect(screen.getByRole("button", { name: /teste/i })).toHaveAttribute("aria-expanded", "false");
  });

  it("retira o destaque do parceiro após três segundos e limpa ao navegar sem âncora", () => {
    vi.useFakeTimers();
    vi.stubGlobal("scrollTo", vi.fn());
    Object.defineProperty(Element.prototype, "scrollIntoView", { configurable: true, value: vi.fn() });
    render(<MemoryRouter initialEntries={["/parceiros#parceiro-teste"]}>
      <Parceiros /><Navigate to="/parceiros" />
    </MemoryRouter>);
    expect(screen.getByTestId("partners")).toHaveAttribute("data-highlight", "parceiro-teste");
    act(() => vi.advanceTimersByTime(3000));
    expect(screen.getByTestId("partners")).toHaveAttribute("data-highlight", "");
    fireEvent.click(screen.getByRole("button", { name: "Navegar" }));
    expect(screen.getByTestId("partners")).toHaveAttribute("data-highlight", "");
    expect(window.scrollTo).toHaveBeenCalledWith(0, 0);
  });

  it("termina a digitação e encerra o cursor quando solicitado", () => {
    vi.useFakeTimers();
    vi.stubGlobal("matchMedia", vi.fn(() => ({ matches: false })));
    const { container } = render(<TextoDigitado texto="Oi" cursorFinal={false} />);
    act(() => vi.advanceTimersByTime(1));
    act(() => vi.advanceTimersByTime(40));
    const animated = container.querySelector('[aria-hidden="true"]');
    expect(animated?.textContent).toBe("Oi");
    expect(animated?.children).toHaveLength(1);
  });
});
