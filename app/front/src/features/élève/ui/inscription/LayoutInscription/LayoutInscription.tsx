import BoutonSquelette from "@/components/_dsfr/BoutonSquelette/BoutonSquelette";
import IndicateurÉtapes from "@/components/_dsfr/IndicateurÉtapes/IndicateurÉtapes";
import Bouton from "@/components/Bouton/Bouton";
import LienInterne from "@/components/Lien/LienInterne/LienInterne";
import Titre from "@/components/Titre/Titre";
import { i18n } from "@/configuration/i18n/i18n";
import {
  actionsÉtapesInscriptionÉlèveStore,
  étapeActuelleÉtapesInscriptionÉlèveStore,
  étapePrécédenteÉtapesInscriptionÉlèveStore,
  étapesÉtapesInscriptionÉlèveStore,
  indexÉtapeActuelleÉtapesInscriptionÉlèveStore,
} from "@/features/élève/store/useÉtapesInscriptionÉlève/useÉtapesInscriptionÉlève";
import { Outlet, useRouterState } from "@tanstack/react-router";
import { useEffect } from "react";

const LayoutInscription = () => {
  const { définirÉtapeActuelle } = actionsÉtapesInscriptionÉlèveStore();
  const étapeActuelle = étapeActuelleÉtapesInscriptionÉlèveStore();
  const étapePrécédente = étapePrécédenteÉtapesInscriptionÉlèveStore();
  const indexÉtapeActuelle = indexÉtapeActuelleÉtapesInscriptionÉlèveStore();
  const étapes = étapesÉtapesInscriptionÉlèveStore();
  const router = useRouterState();

  useEffect(() => {
    définirÉtapeActuelle(router.location.pathname);
  }, [router.location.pathname, définirÉtapeActuelle]);

  return (
    <div className="bg-[--background-alt-beige-gris-galet] h-full">
      <div className="bg-[url('/images-de-fond/inscription.svg')] bg-no-repeat h-full bg-cover">
        <div className="fr-container--fluid">
          <div className="fr-grid-row fr-grid-row--center">
            <div className="bg-[--background-raised-grey] md:max-w-[840px] md:min-w-[740px] md:shadow-md fr-col-12 fr-col-md-8 fr-mt-md-7w fr-mb-md-10w">
              <div className="shadow-md fr-p-2w fr-py-md-3w fr-px-md-10w">
                <IndicateurÉtapes
                  indexÉtapeActuelle={indexÉtapeActuelle ?? 1}
                  étapes={étapes.map((étape) => étape.titreÉtape)}
                />
              </div>
              <div className="fr-py-3w fr-px-2w fr-py-md-6w fr-px-md-8w">
                <div className="*:mb-0">
                  <Titre
                    niveauDeTitre="h1"
                    styleDeTitre="h2"
                  >
                    {étapeActuelle?.titre}
                  </Titre>
                </div>
                <p className="fr-text--lg">{i18n.COMMUN.CHAMPS_OBLIGATOIRES}</p>
                <Outlet />
                <hr />
                <div className={`fr-grid-row ${étapePrécédente ? "justify-between" : "justify-end"}`}>
                  {étapePrécédente && (
                    <LienInterne
                      ariaLabel={i18n.COMMUN.RETOUR}
                      href={étapePrécédente.url}
                      variante="neutre"
                    >
                      <BoutonSquelette
                        icône={{ position: "gauche", classe: "fr-icon-arrow-left-line" }}
                        label={i18n.COMMUN.RETOUR}
                        variante="secondaire"
                      />
                    </LienInterne>
                  )}
                  <Bouton
                    formId={étapeActuelle?.url}
                    icône={{ position: "droite", classe: "fr-icon-arrow-right-line" }}
                    label={i18n.COMMUN.CONTINUER}
                    type="submit"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LayoutInscription;
