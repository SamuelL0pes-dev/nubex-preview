import { useNavigate } from "react-router-dom";
import AboutIntro from "../../components/features/AboutIntro";
import AnimatedCounters from "../../components/features/AnimatedCounters";
import HeroBanner from "../../components/features/HeroBanner";
import Partners from "../../components/features/Partners";
import QuickAccessCards from "../../components/features/QuickAccessCards";

export default function Home() {
  const navegar = useNavigate();

  function abrirParceiros(id: string) {
    navegar(id === "todos" ? "/parceiros" : `/parceiros#parceiro-${id}`);
  }

  return (
    <>
      <HeroBanner />
      <AboutIntro />
      <QuickAccessCards />
      <AnimatedCounters />
      <Partners onSelecionar={abrirParceiros} />
    </>
  );
}