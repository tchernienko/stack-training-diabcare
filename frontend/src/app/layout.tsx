import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Diabcare",
  description: "Suivi des patients diabétiques type 1 sous pompe",
  manifest: "/manifest.json",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="fr">
      <body>{children}</body>
    </html>
  );
}
