import { useState } from "react";
import { useDispatch } from "react-redux";
import { setResults } from "../slices/resultsSlice";
import { useGetAiDataMutation } from "../slices/thirdPartyApiSlice";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMagnifyingGlass } from "@fortawesome/free-solid-svg-icons";
import { toast } from "react-toastify";
//Components
import SearchResults from "./SearchResults";

const generateOptionsConfig = {
  MIN_LENGTH: 10,
  MAX_LENGTH: 50,
  STEP: 10,
};

interface GenerateOptionsConfig {
  MIN_LENGTH: number;
  MAX_LENGTH: number;
  STEP: number;
}

const genertateOptions = (config: GenerateOptionsConfig): JSX.Element[] => {
  const { MIN_LENGTH, MAX_LENGTH, STEP } = config;
  const options: JSX.Element[] = [];
  for (let i = MIN_LENGTH; i <= MAX_LENGTH; i += STEP) {
    options.push(
      <option key={i} value={i}>
        {i}
      </option>
    );
  }
  return options;
};

const Searchbar = () => {
  const [search, setSearch] = useState<string>("");
  const [playlistLength, setPlaylistLength] = useState<number>(
    generateOptionsConfig.MIN_LENGTH
  );
  const [options] = useState<JSX.Element[]>(
    genertateOptions(generateOptionsConfig)
  );

  const dispatch = useDispatch();

  const [getAiData, { isLoading }] = useGetAiDataMutation();

  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearch(e.target.value);
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    getAndSetResults();
  };

  const handleClick = async () => {
    getAndSetResults();
  };

  const getAndSetResults = async () => {
    try {
      const res = await getAiData({
        input: search,
        length: playlistLength,
      }).unwrap();
      dispatch(setResults(res));
    } catch (error) {
      console.error(error);
      toast.error("Error generating playlist: AI Service Unavailable", {
        position: "top-center",
      });
    }
  };

  return (
    <>
      <div className='flex-container-search'>
        <form className='searchbar' onSubmit={handleSubmit}>
          <input
            className='search-input'
            type='text'
            value={search}
            onChange={handleSearch}
            placeholder='Generate a playlist based on your prompt.'
          />
          <FontAwesomeIcon
            className='search-icon'
            icon={faMagnifyingGlass}
            onClick={handleClick}
          />
        </form>
        <form className='playlist-length-select'>
          <label>
            Playlist Length:
            <select
              name='length'
              className='playlist-length'
              value={playlistLength}
              onChange={(e) => setPlaylistLength(parseInt(e.target.value))}
            >
              {options.map((option) => option)}
            </select>
          </label>
        </form>
      </div>
      <SearchResults loading={isLoading} />
    </>
  );
};

export default Searchbar;
