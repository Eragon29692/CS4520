/***
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband4 for more book information.
***/
package edu.neu.madcourse.duyvu.wordgame;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.neu.madcourse.duyvu.R;

public class WGControlFragment extends Fragment {

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      View rootView =
            inflater.inflate(R.layout.wgfragment_control, container, false);
      View main = rootView.findViewById(R.id.wgbutton_main);
      View restart = rootView.findViewById(R.id.wgbutton_restart);

      main.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            getActivity().finish();
         }
      });
      restart.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            ((WGGameActivity) getActivity()).restartGame();
         }
      });
      return rootView;
   }

}