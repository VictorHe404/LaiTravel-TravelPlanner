// components/DayPOISection.tsx
"use client";
import React from "react";
import SearchPOI from "./SearchPOI";
import { POI } from "./types";

interface Props {
  day: number;
  initialPois: POI[];
  city: string;
  itineraryId?: string;
  isActive: boolean;
  onUpdatePois: (day: number, next: POI[]) => void; // Parent updates state
  onSelectDay: (day: number) => void;
}

export default function DayPOISection({
  day,
  initialPois,
  city,
  itineraryId,
  onUpdatePois,
  onSelectDay,
  isActive = false,
}: Props) {
  // Make this a controlled component: always use initialPois from parent
  const pois = initialPois;

  const handlePick = (picked: { name: string; lat: number; lng: number }) => {
    const newPoi: POI = {
      name: picked.name,
      lat: picked.lat,
      lng: picked.lng,
      sequence: pois.length,
      day: day,
    };
    // Bubble up to parent, which will re-render with new initialPois
    onUpdatePois(day, [...pois, newPoi]);
  };

  // Helper to calculate driving time (mock, replace with real API if needed)
  function getDrivingTime(p1: POI, p2: POI): string {
    if (!p1 || !p2) return "";
    const R = 6371; // km
    const dLat = ((p2.lat - p1.lat) * Math.PI) / 180;
    const dLng = ((p2.lng - p1.lng) * Math.PI) / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos((p1.lat * Math.PI) / 180) *
        Math.cos((p2.lat * Math.PI) / 180) *
        Math.sin(dLng / 2) *
        Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c; // km
    const timeMin = Math.round((distance / 40) * 60);
    return `${timeMin || 5} min drive`;
  }

  return (
    <div
      className={`rounded border p-4 ${isActive ? "bg-blue-50" : "bg-white"}`}
    >
      <div className="flex items-center justify-between mb-3">
        <h3 className="font-semibold">Day {day}</h3>
        <button className="text-sm opacity-70" onClick={() => onSelectDay(day)}>
          {isActive ? "Active" : "Set Active"}
        </button>
      </div>

      <SearchPOI
        city={city}
        onPick={handlePick}
        placeholder="Type e.g. 博物馆 / museum…"
      />

      <ol className="mt-3 space-y-2 list-decimal pl-5">
        {pois.map((p, i) => (
          <React.Fragment key={`day-${day}-seq-${p.sequence}`}>
            <li>{p.name}</li>
            {i < pois.length - 1 && (
              <li className="ml-2 list-none flex items-center text-blue-600 text-sm">
                <span className="inline-block mr-2">🚗</span>
                {getDrivingTime(p, pois[i + 1])}
              </li>
            )}
          </React.Fragment>
        ))}
      </ol>
    </div>
  );
}
