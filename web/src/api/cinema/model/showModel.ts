export interface ShowItem {
  id: string;
  movieTitle: string;
  roomName: string;
  startTime: string;
  basePrice: number;
  availableSeats: number;
  totalSeats: number;
}

export type ShowResult = {
  success: boolean;
  data: Array<ShowItem>;
};
