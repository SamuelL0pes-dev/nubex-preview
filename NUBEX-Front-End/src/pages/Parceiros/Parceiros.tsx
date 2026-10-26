import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import PartnersList from "../../components/features/Partners/PartnersList";

export default function Parceiros() {
  const { hash } = useLocation();
  return <ParceirosConteudo key={hash} hash={hash} />;
}

function ParceirosConteudo({ hash }: { hash: string }) {
  const [destacado, setDestacado] = useState(() => hash.replace("#", ""));

  useEffect(() => {
    const id = hash.replace("#", "");

    if (!id) {
      window.scrollTo(0, 0);
      return;
    }

    const alvo = document.getElementById(id);
    alvo?.scrollIntoView({ behavior: "smooth", block: "center" });

    const relogio = setTimeout(() => setDestacado(""), 3000);

    return () => clearTimeout(relogio);
  }, [hash]);

  return <PartnersList destacado={destacado} />;
}