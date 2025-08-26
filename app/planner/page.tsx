"use client";
import React, { useState, useEffect, useMemo } from "react";
import PlannerForm from "@/components/PlannerForm";
import DayPOISection from "@/components/DayPOISection";
import PlannerMap from "@/components/PlannerMap";
import SavePlanButton from "@/components/SavePlanButton";
import { POI, DayPOI } from "@/components/types";
import { useRouter } from "next/navigation";
import AIChatBar from "@/components/AIChatBar";   // ⬅️ import the new chat component

export default function PlannerPage() {
    const router = useRouter();
    const [city, setCity] = useState("Beijing");
    const [days, setDays] = useState(1);
    const [dayPOIs, setDayPOIs] = useState<DayPOI[]>([]);
    const [selectedDay, setSelectedDay] = useState<number | null>(1);

    useEffect(() => {
        setDayPOIs((prev) => {
            const updated: DayPOI[] = [];
            for (let i = 1; i <= days; i++) {
                const existing = prev.find((d) => d.day === i);
                updated.push({ day: i, pois: existing?.pois ?? [] });
            }
            return updated;
        });
        setSelectedDay(1);
    }, [days]);

    const updatePOIsForDay = (day: number, newPois: POI[]) => {
        setDayPOIs((prev) =>
            prev.map((d) =>
                d.day === day
                    ? { ...d, pois: newPois.map((p, i) => ({ ...p, sequence: i + 1 })) }
                    : d
            )
        );
    };

    const allPois = useMemo(
        () =>
            dayPOIs.flatMap((d) =>
                d.pois.map((poi, i) => ({
                    ...poi,
                    day: d.day,
                    sequence: i + 1,
                }))
            ),
        [dayPOIs]
    );

    const currentDayPois =
        selectedDay ? dayPOIs.find((d) => d.day === selectedDay)?.pois ?? [] : [];

    return (
        <main className="p-6 max-w-6xl mx-auto">
            <h1 className="text-2xl font-bold mb-4 text-center">
                Itinerary Planner - {city}
            </h1>

            <div className="grid md:grid-cols-2 gap-6">
                {/* Left: Form + Days */}
                <div>
                    <PlannerForm
                        city={city}
                        days={days}
                        onCityChange={setCity}
                        onDaysChange={setDays}
                    />

                    <div className="mt-6 space-y-6">
                        <h2 className="text-lg font-semibold">Your Itinerary by Day</h2>

                        {dayPOIs.map(({ day, pois }) => (
                            <DayPOISection
                                key={day}
                                day={day}
                                city={city}
                                initialPois={pois}
                                onUpdatePois={(d, updated) => updatePOIsForDay(d, updated)}
                                onSelectDay={(d) => setSelectedDay(d)}
                                isActive={selectedDay === day}
                            />
                        ))}

                        <SavePlanButton
                            planData={{ city, days, pois: allPois }}
                            onPlanSaved={(saved) => {
                                const id = (saved as any)?.plan?.id ?? (saved as any)?.id;
                                if (id) router.push(`/planner/${id}`);
                                else console.warn("Saved, but no id returned from backend.");
                            }}
                        />
                    </div>
                </div>

                {/* Right: Map */}
                <div className="h-[500px]">
                    <PlannerMap city={city} pois={currentDayPois} />
                </div>
            </div>

            {/* Floating AI Chat Bar */}
            <AIChatBar
                city={city}
                days={days}
                selectedDay={selectedDay}
                dayPOIs={dayPOIs}
            />
        </main>
    );
}
