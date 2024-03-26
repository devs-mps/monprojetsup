/* prettier-ignore-start */

/* eslint-disable */

// @ts-nocheck

// noinspection JSUnusedGlobalSymbols

// This file is auto-generated by TanStack Router

import { createFileRoute } from '@tanstack/react-router'

// Import Routes

import { Route as rootRoute } from './routes/__root'
import { Route as LayoutInscriptionImport } from './routes/_layout-inscription'
import { Route as ArticlesIndexImport } from './routes/articles/index'
import { Route as LayoutInscriptionInscriptionMonProjetIndexImport } from './routes/_layout-inscription/inscription/mon-projet/index'

// Create Virtual Routes

const IndexLazyImport = createFileRoute('/')()

// Create/Update Routes

const LayoutInscriptionRoute = LayoutInscriptionImport.update({
  id: '/_layout-inscription',
  getParentRoute: () => rootRoute,
} as any)

const IndexLazyRoute = IndexLazyImport.update({
  path: '/',
  getParentRoute: () => rootRoute,
} as any).lazy(() => import('./routes/index.lazy').then((d) => d.Route))

const ArticlesIndexRoute = ArticlesIndexImport.update({
  path: '/articles/',
  getParentRoute: () => rootRoute,
} as any).lazy(() =>
  import('./routes/articles/index.lazy').then((d) => d.Route),
)

const LayoutInscriptionInscriptionMonProjetIndexRoute =
  LayoutInscriptionInscriptionMonProjetIndexImport.update({
    path: '/inscription/mon-projet/',
    getParentRoute: () => LayoutInscriptionRoute,
  } as any)

// Populate the FileRoutesByPath interface

declare module '@tanstack/react-router' {
  interface FileRoutesByPath {
    '/': {
      preLoaderRoute: typeof IndexLazyImport
      parentRoute: typeof rootRoute
    }
    '/_layout-inscription': {
      preLoaderRoute: typeof LayoutInscriptionImport
      parentRoute: typeof rootRoute
    }
    '/articles/': {
      preLoaderRoute: typeof ArticlesIndexImport
      parentRoute: typeof rootRoute
    }
    '/_layout-inscription/inscription/mon-projet/': {
      preLoaderRoute: typeof LayoutInscriptionInscriptionMonProjetIndexImport
      parentRoute: typeof LayoutInscriptionImport
    }
  }
}

// Create and export the route tree

export const routeTree = rootRoute.addChildren([
  IndexLazyRoute,
  LayoutInscriptionRoute.addChildren([
    LayoutInscriptionInscriptionMonProjetIndexRoute,
  ]),
  ArticlesIndexRoute,
])

/* prettier-ignore-end */
