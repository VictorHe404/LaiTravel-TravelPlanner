// app/itinerary/[id]/page.tsx
import React from "react";
import { notFound } from "next/navigation";
import { cookies } from "next/headers";
import { POI, DayPOI } from "@/components/types";
import { groupPOIsByDay } from "@/components/utils/groupByDay";

export const dynamic = "force-dynamic";

interface PageProps {
  params: { id: string | string[] };
  searchParams?: Record<string, string | string[] | undefined>;
}

type ItineraryResponse = {
  id: string | number;
  city: string;
  pois: POI[];
};

// ---- Config ----
const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080";
const SHOW_PATH =
    process.env.NEXT_PUBLIC_SHOW_ITINERARY_PATH ?? "/api/itinerary";
const GMAPS_KEY = process.env.GOOGLE_MAPS_API_KEY; // server-only
type Mode = "driving" | "walking" | "bicycling" | "transit";

// ---- Routes API helpers (replaces Distance Matrix) ----
type LegInfo = { distanceText: string; durationText: string };
type LegsResult = { legs: LegInfo[]; error?: string };

function fmtKm(meters?: number): string {
  if (typeof meters !== "number") return "—";
  if (meters < 1000) return `${Math.round(meters)} m`;
  return `${(meters / 1000).toFixed(1)} km`;
}

function fmtDurationRFC3339(d?: string): string {
  if (!d) return "—";
  // Routes API duration commonly comes as "1234s" or "PT23M15S"
  const secMatch = d.match(/^(\d+)s$/i);
  let total = 0;
  if (secMatch) {
    total = parseInt(secMatch[1], 10);
  } else {
    const h = /(\d+)H/.exec(d)?.[1];
    const m = /(\d+)M/.exec(d)?.[1];
    const s = /(\d+)S/.exec(d)?.[1];
    total =
        (h ? parseInt(h, 10) * 3600 : 0) +
        (m ? parseInt(m, 10) * 60 : 0) +
        (s ? parseInt(s, 10) : 0);
  }
  const hrs = Math.floor(total / 3600);
  const mins = Math.round((total % 3600) / 60);
  if (hrs) return `${hrs} hr ${mins} min`;
  return `${mins} min`;
}

function toRoutesMode(mode: Mode) {
  switch (mode) {
    case "walking":
      return "WALK";
    case "bicycling":
      return "BICYCLE";
    case "transit":
      return "TRANSIT";
    default:
      return "DRIVE";
  }
}

async function fetchOneLeg(
    origin: { lat: number; lng: number },
    dest: { lat: number; lng: number },
    mode: Mode,
    apiKey: string
): Promise<LegInfo> {
  const url = "https://routes.googleapis.com/directions/v2:computeRoutes";
  const body = {
    origin: { location: { latLng: { latitude: origin.lat, longitude: origin.lng } } },
    destination: { location: { latLng: { latitude: dest.lat, longitude: dest.lng } } },
    travelMode: toRoutesMode(mode), // DRIVE | WALK | BICYCLE | TRANSIT
    computeTravelSummary: true
  };

  const r = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-Goog-Api-Key": apiKey,
      "X-Goog-FieldMask": "routes.duration,routes.distanceMeters"
    },
    body: JSON.stringify(body)
  });

  if (!r.ok) return { distanceText: "—", durationText: "—" };

  const data = await r.json();
  const route = data?.routes?.[0];
  const distanceMeters = route?.distanceMeters as number | undefined;
  const duration = route?.duration as string | undefined;

  return {
    distanceText: fmtKm(distanceMeters),
    durationText: fmtDurationRFC3339(duration)
  };
}

async function getLegsDistanceTime(pois: POI[], mode: Mode): Promise<LegsResult> {
  const coords = pois.filter(
      (p) => typeof p.lat === "number" && typeof p.lng === "number"
  );
  const empty: LegsResult = {
    legs: Array(Math.max(0, coords.length - 1)).fill({
      distanceText: "—",
      durationText: "—",
    }),
  };

  if (!GMAPS_KEY) return { ...empty, error: "GOOGLE_MAPS_API_KEY missing" };
  if (coords.length < 2) return empty;

  try {
    // One Routes API call per leg; do them in parallel.
    const tasks = coords.slice(0, -1).map((o, i) =>
        fetchOneLeg(
            { lat: o.lat as number, lng: o.lng as number },
            { lat: coords[i + 1].lat as number, lng: coords[i + 1].lng as number },
            mode,
            GMAPS_KEY
        )
    );
    const legs = await Promise.all(tasks);
    return { legs };
  } catch (e: any) {
    return { ...empty, error: `Routes API error: ${e?.message || e}` };
  }
}

// ---- Page ----
export default async function Page({ params, searchParams }: PageProps) {
  const id = Array.isArray(params.id) ? params.id[0] : params.id;

  // read travel mode from URL (?mode=walking|bicycling|transit|driving)
  const urlMode = (Array.isArray(searchParams?.mode)
      ? searchParams?.mode[0]
      : searchParams?.mode) as Mode | undefined;
  const mode: Mode =
      urlMode && ["driving", "walking", "bicycling", "transit"].includes(urlMode)
          ? urlMode
          : "driving";

  // forward cookies/JWT to backend
  const cookieStore = cookies();
  const cookieHeader = cookieStore.getAll().map(c => `${c.name}=${c.value}`).join("; ");
  const jwt = cookieStore.get("jwt")?.value;

  const url = `${API_BASE}${SHOW_PATH}/${encodeURIComponent(id)}`;
  const res = await fetch(url, {
    cache: "no-store",
    headers: {
      Cookie: cookieHeader,
      ...(jwt ? { Authorization: `Bearer ${jwt}` } : {}),
      Accept: "application/json",
    },
  });

  if (res.status === 404) notFound();
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`Failed to fetch itinerary (${res.status}): ${text || "unknown"}`);
  }

  let data: Partial<ItineraryResponse> = {};
  try {
    data = (await res.json()) as Partial<ItineraryResponse>;
  } catch {}

  const city = data.city ?? "Beijing";
  const pois: POI[] = Array.isArray(data.pois) ? data.pois : [];
  const byDay: DayPOI[] = groupPOIsByDay(pois);

  // Fetch legs per day (server-side) using Routes API
  const resultsPerDay = await Promise.all(byDay.map(d => getLegsDistanceTime(d.pois, mode)));

  return (
      <div className="max-w-4xl mx-auto p-6 space-y-6">
        <header className="flex items-center justify-between">
          <h1 className="text-2xl font-semibold">This is your plan</h1>
          <div className="flex items-center gap-3">
            <span className="text-gray-600">{city}</span>
            <nav className="text-sm text-blue-600">
              <a href={`?mode=driving`}   className={mode === "driving"   ? "font-semibold" : ""}>Driving</a>
              <span className="mx-1">·</span>
              <a href={`?mode=walking`}   className={mode === "walking"   ? "font-semibold" : ""}>Walking</a>
              <span className="mx-1">·</span>
              <a href={`?mode=bicycling`} className={mode === "bicycling" ? "font-semibold" : ""}>Bicycling</a>
              <span className="mx-1">·</span>
              <a href={`?mode=transit`}   className={mode === "transit"   ? "font-semibold" : ""}>Transit</a>
            </nav>
          </div>
        </header>

        {byDay.map(({ day, pois }, idx) => {
          const result = resultsPerDay[idx];
          const legs = result?.legs ?? [];

          return (
              <section key={day} className="rounded-xl border p-4 bg-white">
                <h2 className="font-medium mb-3">Day {day}</h2>

                {result?.error && (
                    <p className="mb-3 text-sm text-amber-700">
                      Routes API: {result.error}
                    </p>
                )}

                {pois.length === 0 ? (
                    <p className="text-sm text-gray-500">No POIs for this day.</p>
                ) : (
                    <ol className="list-decimal pl-5 space-y-2">
                      {pois.map((p, i) => (
                          <li key={i} className="text-gray-800">
                            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-1">
                      <span>
                        {p.name}
                        {typeof p.lat === "number" && typeof p.lng === "number" && (
                            <span className="text-gray-500 text-sm">
                            {" "}({p.lat.toFixed(5)}, {p.lng.toFixed(5)})
                          </span>
                        )}
                      </span>

                              {/* Leg info between this POI and the next one */}
                              {i < pois.length - 1 ? (
                                  <span className="text-sm text-blue-600">
                          → {legs[i]?.distanceText ?? "—"} · {legs[i]?.durationText ?? "—"}
                        </span>
                              ) : null}
                            </div>
                          </li>
                      ))}
                    </ol>
                )}

                {!GMAPS_KEY && (
                    <p className="mt-3 text-xs text-amber-600">
                      Tip: Add <code>GOOGLE_MAPS_API_KEY</code> to <code>.env.local</code> to enable distance &amp; time.
                    </p>
                )}
              </section>
          );
        })}
      </div>
  );
}
