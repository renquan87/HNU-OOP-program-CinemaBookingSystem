export type ShowStatus = "UPCOMING" | "HISTORY";

export interface ShowItem {
  id: string;
  movieTitle: string;
  roomName: string;
  roomId: string;
  movieId: string;
  startTime: string;
  basePrice: number;
  availableSeats: number;
  totalSeats: number;
  status?: ShowStatus;
}

export interface ShowListPayload {
  upcomingShows: ShowItem[];
  historyShows: ShowItem[];
  serverTime: string;
}

export type ShowResult = {
  success: boolean;
  data: ShowListPayload;
};
