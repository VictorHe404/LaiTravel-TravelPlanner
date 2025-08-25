// components/PlannerMap.tsx
"use client";
import React, { useEffect, useRef, useState } from "react";
import {
  GoogleMap,
  Marker,
  DirectionsRenderer,
  useLoadScript,
  Libraries,
} from "@react-google-maps/api";
import type { POI } from "./types";

interface PlannerMapProps {
  pois: POI[];
  city?: string;
}

const containerStyle = { width: "100%", height: "500px" };
const libraries: Libraries = ["places"]; // ✅ stable

const BEIJING_BOUNDS = {
  sw: { lat: 39.442758, lng: 115.423124 },
  ne: { lat: 41.060426, lng: 117.512372 },
};

export default function PlannerMap({ city, pois }: PlannerMapProps) {
  const { isLoaded, loadError } = useLoadScript({
    googleMapsApiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY!,
    libraries,
  });

  const [mapCenter, setMapCenter] = useState({ lat: 39.9042, lng: 116.4074 });
  const [mapInstance, setMapInstance] = useState<google.maps.Map | null>(null);
  const [markerPositions, setMarkerPositions] = useState<
    google.maps.LatLngLiteral[]
  >([]);
  const [directions, setDirections] =
    useState<google.maps.DirectionsResult | null>(null);
  const requestVersion = useRef(0);

  const isWithinBeijing = (loc: google.maps.LatLng) =>
    loc.lat() >= BEIJING_BOUNDS.sw.lat &&
    loc.lat() <= BEIJING_BOUNDS.ne.lat &&
    loc.lng() >= BEIJING_BOUNDS.sw.lng &&
    loc.lng() <= BEIJING_BOUNDS.ne.lng;

  // Geocode city when map and API loaded
  useEffect(() => {
    if (!isLoaded || !mapInstance || !city) return;
    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({ address: city }, (results, status) => {
      if (status === "OK" && results?.[0]?.geometry?.location) {
        const loc = results[0].geometry.location;
        setMapCenter({ lat: loc.lat(), lng: loc.lng() });
      }
    });
  }, [isLoaded, mapInstance, city]);

  // Resolve POIs -> marker positions
  useEffect(() => {
    if (!isLoaded || !mapInstance || !pois.length) {
      setMarkerPositions([]);
      return;
    }

    requestVersion.current += 1;
    const current = requestVersion.current;

    const service = new google.maps.places.PlacesService(mapInstance);
    const temp: (google.maps.LatLngLiteral | null)[] = new Array(
      pois.length
    ).fill(null);
    let done = 0;

    pois.forEach((poi, i) => {
      service.findPlaceFromQuery(
        {
          query: poi.name,
          fields: ["geometry"],
          locationBias: {
            south: BEIJING_BOUNDS.sw.lat,
            west: BEIJING_BOUNDS.sw.lng,
            north: BEIJING_BOUNDS.ne.lat,
            east: BEIJING_BOUNDS.ne.lng,
          },
        },
        (results, status) => {
          if (requestVersion.current !== current) return;
          done++;

          const loc = results?.[0]?.geometry?.location || null;
          if (
            status === google.maps.places.PlacesServiceStatus.OK &&
            loc &&
            isWithinBeijing(loc)
          ) {
            temp[i] = { lat: loc.lat(), lng: loc.lng() };
          }

          if (done === pois.length && requestVersion.current === current) {
            setMarkerPositions(
              temp.filter((p): p is google.maps.LatLngLiteral => !!p)
            );
          }
        }
      );
    });
  }, [isLoaded, mapInstance, pois]);

  // Get directions route when marker positions change
  useEffect(() => {
    if (!isLoaded || !mapInstance || markerPositions.length < 2) {
      setDirections(null);
      return;
    }
    const directionsService = new google.maps.DirectionsService();
    const waypoints = markerPositions
      .slice(1, -1)
      .map((pos) => ({ location: pos, stopover: true }));
    directionsService.route(
      {
        origin: markerPositions[0],
        destination: markerPositions[markerPositions.length - 1],
        waypoints,
        travelMode: google.maps.TravelMode.DRIVING,
        optimizeWaypoints: false,
      },
      (result, status) => {
        if (status === google.maps.DirectionsStatus.OK && result) {
          setDirections(result);
        } else {
          setDirections(null);
        }
      }
    );
  }, [isLoaded, mapInstance, markerPositions]);

  if (loadError) return <div>Map failed to load.</div>;
  if (!isLoaded) return <div>Loading…</div>;

  return (
    <div style={containerStyle}>
      <GoogleMap
        mapContainerStyle={{ width: "100%", height: "100%" }}
        center={mapCenter}
        zoom={12}
        onLoad={(m) => setMapInstance(m)}
      >
        {markerPositions.map((pos, idx) => (
          <Marker
            key={idx}
            position={pos}
            label={{
              text: String(idx + 1),
              color: "#fff",
              fontSize: "12px",
              fontWeight: "bold",
            }}
          />
        ))}
        {directions && (
          <DirectionsRenderer
            directions={directions}
            options={{ suppressMarkers: true }}
          />
        )}
      </GoogleMap>
    </div>
  );
}
