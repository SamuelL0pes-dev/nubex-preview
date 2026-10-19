import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import PartnersList from "../../components/features/Partners/PartnersList";

export default function Parceiros() {
  const { hash } = useLocation();
  const [destacado, setDestacado] = useState("");

  useEffect(() => {
    const id = hash.replace("#", "");

    if (!id) {
      setDestacado("");
      window.scrollTo(0, 0);
      return;
    }

    setDestacado(id);

    const alvo = document.getElementById(id);
    alvo?.scrollIntoView({ behavior: "smooth", block: "center" });

    const relogio = setTimeout(() => setDestacado(""), 3000);

    return () => clearTimeout(relogio);
  }, [hash]);

  return <PartnersList destacado={destacado} />;
}