import { Routes, Route, useLocation } from "react-router-dom";
import Header from "./components/layout/Header";
import Footer from "./components/layout/Footer";
import Breadcrumb from "./components/layout/Breadcrumb";
import ScrollToTop from "./components/layout/ScrollToTop";

import Home from "./pages/Home/Home";
import QuemSomos from "./pages/QuemSomos/QuemSomos";
import MissaoVisaoValores from "./pages/MissaoVisaoValores/MissaoVisaoValores";
import UnidadesList from "./pages/Unidades/UnidadesList";
import UnidadeDetail from "./pages/Unidades/UnidadeDetail";
import Servicos from "./pages/Servicos/Servicos";
import Diferenciais from "./pages/Diferenciais/Diferenciais";
import Agendamento from "./pages/Agendamento/Agendamento";
import Avisos from "./pages/Avisos/Avisos";
import FAQ from "./pages/FAQ/FAQ";
import Contato from "./pages/Contato/Contato";
import Parceiros from "./pages/Parceiros/Parceiros";
import Projetos from "./pages/Projetos/Projetos";
import Noticias from "./pages/Noticias/Noticias";
import Login from "./pages/Login/Login";
import NotFound from "./pages/NotFound/NotFound";

export default function App() {
  const location = useLocation();
  const isLoginPage = location.pathname === "/login";

  return (
    <div style={{ display: "flex", flexDirection: "column", minHeight: "100vh" }}>
      <ScrollToTop />
      {!isLoginPage && <Header />}
      {!isLoginPage && <Breadcrumb />}

      <main style={{ flex: 1 }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/quem-somos" element={<QuemSomos />} />
          <Route path="/missao-visao-valores" element={<MissaoVisaoValores />} />
          <Route path="/unidades" element={<UnidadesList />} />
          <Route path="/unidades/:slug" element={<UnidadeDetail />} />
          <Route path="/servicos" element={<Servicos />} />
          <Route path="/diferenciais" element={<Diferenciais />} />
          <Route path="/agendamento" element={<Agendamento />} />
          <Route path="/avisos" element={<Avisos />} />
          <Route path="/faq" element={<FAQ />} />
          <Route path="/contato" element={<Contato />} />
          <Route path="/parceiros" element={<Parceiros />} />
          <Route path="/projetos" element={<Projetos />} />
          <Route path="/noticias" element={<Noticias />} />
          <Route path="/login" element={<Login />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>

      {!isLoginPage && <Footer />}
    </div>
  );
}
