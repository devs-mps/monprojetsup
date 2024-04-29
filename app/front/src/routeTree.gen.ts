/* prettier-ignore-start */

/* eslint-disable */

// @ts-nocheck

// noinspection JSUnusedGlobalSymbols

// This file is auto-generated by TanStack Router

import { createFileRoute } from '@tanstack/react-router'

// Import Routes

import { Route as rootRoute } from './routes/__root'
import { Route as InscriptionImport } from './routes/_inscription'
import { Route as RechercheIndexImport } from './routes/recherche/index'
import { Route as InscriptionInscriptionScolariteIndexImport } from './routes/_inscription/inscription/scolarite/index'
import { Route as InscriptionInscriptionProjetIndexImport } from './routes/_inscription/inscription/projet/index'
import { Route as InscriptionInscriptionMetiersIndexImport } from './routes/_inscription/inscription/metiers/index'
import { Route as InscriptionInscriptionInteretsIndexImport } from './routes/_inscription/inscription/interets/index'
import { Route as InscriptionInscriptionFormationsIndexImport } from './routes/_inscription/inscription/formations/index'
import { Route as InscriptionInscriptionEtudeIndexImport } from './routes/_inscription/inscription/etude/index'
import { Route as InscriptionInscriptionDomainesIndexImport } from './routes/_inscription/inscription/domaines/index'

// Create Virtual Routes

const IndexLazyImport = createFileRoute('/')()
const InscriptionInscriptionConfirmationIndexLazyImport = createFileRoute(
  '/_inscription/inscription/confirmation/',
)()

// Create/Update Routes

const InscriptionRoute = InscriptionImport.update({
  id: '/_inscription',
  getParentRoute: () => rootRoute,
} as any)

const IndexLazyRoute = IndexLazyImport.update({
  path: '/',
  getParentRoute: () => rootRoute,
} as any).lazy(() => import('./routes/index.lazy').then((d) => d.Route))

const RechercheIndexRoute = RechercheIndexImport.update({
  path: '/recherche/',
  getParentRoute: () => rootRoute,
} as any).lazy(() =>
  import('./routes/recherche/index.lazy').then((d) => d.Route),
)

const InscriptionInscriptionConfirmationIndexLazyRoute =
  InscriptionInscriptionConfirmationIndexLazyImport.update({
    path: '/inscription/confirmation/',
    getParentRoute: () => InscriptionRoute,
  } as any).lazy(() =>
    import('./routes/_inscription/inscription/confirmation/index.lazy').then(
      (d) => d.Route,
    ),
  )

const InscriptionInscriptionScolariteIndexRoute =
  InscriptionInscriptionScolariteIndexImport.update({
    path: '/inscription/scolarite/',
    getParentRoute: () => InscriptionRoute,
  } as any).lazy(() =>
    import('./routes/_inscription/inscription/scolarite/index.lazy').then(
      (d) => d.Route,
    ),
  )

const InscriptionInscriptionProjetIndexRoute =
  InscriptionInscriptionProjetIndexImport.update({
    path: '/inscription/projet/',
    getParentRoute: () => InscriptionRoute,
  } as any).lazy(() =>
    import('./routes/_inscription/inscription/projet/index.lazy').then(
      (d) => d.Route,
    ),
  )

const InscriptionInscriptionMetiersIndexRoute =
  InscriptionInscriptionMetiersIndexImport.update({
    path: '/inscription/metiers/',
    getParentRoute: () => InscriptionRoute,
  } as any).lazy(() =>
    import('./routes/_inscription/inscription/metiers/index.lazy').then(
      (d) => d.Route,
    ),
  )

const InscriptionInscriptionInteretsIndexRoute =
  InscriptionInscriptionInteretsIndexImport.update({
    path: '/inscription/interets/',
    getParentRoute: () => InscriptionRoute,
  } as any).lazy(() =>
    import('./routes/_inscription/inscription/interets/index.lazy').then(
      (d) => d.Route,
    ),
  )

const InscriptionInscriptionFormationsIndexRoute =
  InscriptionInscriptionFormationsIndexImport.update({
    path: '/inscription/formations/',
    getParentRoute: () => InscriptionRoute,
  } as any).lazy(() =>
    import('./routes/_inscription/inscription/formations/index.lazy').then(
      (d) => d.Route,
    ),
  )

const InscriptionInscriptionEtudeIndexRoute =
  InscriptionInscriptionEtudeIndexImport.update({
    path: '/inscription/etude/',
    getParentRoute: () => InscriptionRoute,
  } as any).lazy(() =>
    import('./routes/_inscription/inscription/etude/index.lazy').then(
      (d) => d.Route,
    ),
  )

const InscriptionInscriptionDomainesIndexRoute =
  InscriptionInscriptionDomainesIndexImport.update({
    path: '/inscription/domaines/',
    getParentRoute: () => InscriptionRoute,
  } as any).lazy(() =>
    import('./routes/_inscription/inscription/domaines/index.lazy').then(
      (d) => d.Route,
    ),
  )

// Populate the FileRoutesByPath interface

declare module '@tanstack/react-router' {
  interface FileRoutesByPath {
    '/': {
      preLoaderRoute: typeof IndexLazyImport
      parentRoute: typeof rootRoute
    }
    '/_inscription': {
      preLoaderRoute: typeof InscriptionImport
      parentRoute: typeof rootRoute
    }
    '/recherche/': {
      preLoaderRoute: typeof RechercheIndexImport
      parentRoute: typeof rootRoute
    }
    '/_inscription/inscription/domaines/': {
      preLoaderRoute: typeof InscriptionInscriptionDomainesIndexImport
      parentRoute: typeof InscriptionImport
    }
    '/_inscription/inscription/etude/': {
      preLoaderRoute: typeof InscriptionInscriptionEtudeIndexImport
      parentRoute: typeof InscriptionImport
    }
    '/_inscription/inscription/formations/': {
      preLoaderRoute: typeof InscriptionInscriptionFormationsIndexImport
      parentRoute: typeof InscriptionImport
    }
    '/_inscription/inscription/interets/': {
      preLoaderRoute: typeof InscriptionInscriptionInteretsIndexImport
      parentRoute: typeof InscriptionImport
    }
    '/_inscription/inscription/metiers/': {
      preLoaderRoute: typeof InscriptionInscriptionMetiersIndexImport
      parentRoute: typeof InscriptionImport
    }
    '/_inscription/inscription/projet/': {
      preLoaderRoute: typeof InscriptionInscriptionProjetIndexImport
      parentRoute: typeof InscriptionImport
    }
    '/_inscription/inscription/scolarite/': {
      preLoaderRoute: typeof InscriptionInscriptionScolariteIndexImport
      parentRoute: typeof InscriptionImport
    }
    '/_inscription/inscription/confirmation/': {
      preLoaderRoute: typeof InscriptionInscriptionConfirmationIndexLazyImport
      parentRoute: typeof InscriptionImport
    }
  }
}

// Create and export the route tree

export const routeTree = rootRoute.addChildren([
  IndexLazyRoute,
  InscriptionRoute.addChildren([
    InscriptionInscriptionDomainesIndexRoute,
    InscriptionInscriptionEtudeIndexRoute,
    InscriptionInscriptionFormationsIndexRoute,
    InscriptionInscriptionInteretsIndexRoute,
    InscriptionInscriptionMetiersIndexRoute,
    InscriptionInscriptionProjetIndexRoute,
    InscriptionInscriptionScolariteIndexRoute,
    InscriptionInscriptionConfirmationIndexLazyRoute,
  ]),
  RechercheIndexRoute,
])

/* prettier-ignore-end */
