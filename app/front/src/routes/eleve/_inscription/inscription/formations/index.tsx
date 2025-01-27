import { élèveQueryOptions } from "@/features/élève/ui/options";
import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/eleve/_inscription/inscription/formations/")({
  loader: async ({ context: { queryClient } }) => await queryClient.ensureQueryData(élèveQueryOptions),
});
