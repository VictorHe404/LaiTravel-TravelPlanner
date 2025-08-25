// app/itinerary/[id]/page.tsx
import React from "react";
import { notFound } from "next/navigation";
import { cookies } from "next/headers";
import { POI, DayPOI } from "@/components/types";
import { groupPOIsByDay } from "@/components/utils/groupByDay";

export const dynamic = "force-dynamic";

interface PageProps {
  params: { id: string | string[] };
}

type ItineraryResponse = {
  id: string | number;
  city: string;
  pois: POI[];
};

// Env-configurable API (adjust defaults to your backend)
const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080";
// e.g. "/api/itinerary" OR "/api/itineraries" OR "/itinerary"
const SHOW_PATH =
  process.env.NEXT_PUBLIC_SHOW_ITINERARY_PATH ?? "/api/itinerary";

export default async function Page({ params }: PageProps) {
  const id = Array.isArray(params.id) ? params.id[0] : params.id;

  // Build Cookie header from the incoming request (server component fetch)
  const cookieStore = cookies();
  const cookieHeader = cookieStore
    .getAll()
    .map((c) => `${c.name}=${c.value}`)
    .join("; ");

  // If you also keep a JWT cookie and your backend expects Bearer auth:
  const jwt = cookieStore.get("jwt")?.value;

  const url = `${API_BASE}${SHOW_PATH}/${encodeURIComponent(id)}`;

  const res = await fetch(url, {
    cache: "no-store",
    headers: {
      Cookie: cookieHeader, // forward session/JWT cookies
      ...(jwt ? { Authorization: `Bearer ${jwt}` } : {}),
      Accept: "application/json",
    },
  });

  if (res.status === 404) {
    notFound();
  }

  if (!res.ok) {
    // read body text safely for better error messages
    const text = await res.text().catch(() => "");
    throw new Error(
      `Failed to fetch itinerary (${res.status}): ${text || "unknown"}`
    );
  }

  let data: Partial<ItineraryResponse> = {};
  try {
    data = (await res.json()) as Partial<ItineraryResponse>;
  } catch {
    // backend returned 200 with no/invalid JSON
  }

  const city = data.city ?? "Beijing";
  const pois: POI[] = Array.isArray(data.pois) ? data.pois : [];
  const byDay: DayPOI[] = groupPOIsByDay(pois);

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">This is your plan</h1>
        <span className="text-gray-600">{city}</span>
      </header>

      {byDay.map(({ day, pois }) => (
        <section key={day} className="rounded-xl border p-4 bg-white">
          <h2 className="font-medium mb-3">Day {day}</h2>

          {pois.length === 0 ? (
            <p className="text-sm text-gray-500">No POIs for this day.</p>
          ) : (
            <ol className="list-decimal pl-5 space-y-1">
              {pois.map((p, i) => (
                <li key={i} className="text-gray-800">
                  {p.name}
                  {typeof p.lat === "number" && typeof p.lng === "number" ? (
                    <span className="text-gray-500 text-sm">
                      {" "}
                      ({p.lat.toFixed(5)}, {p.lng.toFixed(5)})
                    </span>
                  ) : null}
                </li>
              ))}
            </ol>
          )}
        </section>
      ))}
    </div>
  );
}
